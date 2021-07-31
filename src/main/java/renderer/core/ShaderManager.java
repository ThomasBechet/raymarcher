package renderer.core;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.shaderc.ShadercIncludeResolve;
import org.lwjgl.util.shaderc.ShadercIncludeResult;
import org.lwjgl.util.shaderc.ShadercIncludeResultRelease;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.util.shaderc.Shaderc.*;
import static org.lwjgl.vulkan.VK11.*;

public class ShaderManager {

    public static boolean INCLUDE_DEBUG_SYMBOLS = true;

    private Context ctx;
    private List<Long> shaderModules;

    private static int vulkanStageToShadercKind(int stage) {
        switch (stage) {
            case VK_SHADER_STAGE_VERTEX_BIT:
                return shaderc_vertex_shader;
            case VK_SHADER_STAGE_FRAGMENT_BIT:
                return shaderc_fragment_shader;
            default:
                throw new IllegalArgumentException("Stage: " + stage);
        }
    }

    private static ByteBuffer glslToSpirv(String filename, int vulkanStage, MemoryStack stack) throws IOException, URISyntaxException {
        InputStream inputStream = ShaderManager.class.getClassLoader().getResourceAsStream(filename);
        ByteBuffer bytes = stack.bytes(inputStream.readAllBytes());
        long compiler = shaderc_compiler_initialize();
        long options = shaderc_compile_options_initialize();
        ShadercIncludeResolve resolver;
        ShadercIncludeResultRelease releaser;
        shaderc_compile_options_set_target_env(options, shaderc_target_env_vulkan, shaderc_env_version_vulkan_1_1);
        shaderc_compile_options_set_target_spirv(options, shaderc_spirv_version_1_0);
        shaderc_compile_options_set_optimization_level(options, shaderc_optimization_level_performance);
        if (INCLUDE_DEBUG_SYMBOLS) {
            shaderc_compile_options_set_generate_debug_info(options);
        }
        shaderc_compile_options_set_include_callbacks(options, resolver = new ShadercIncludeResolve() {
            @Override
            public long invoke(long user_data, long requested_source, int type, long requesting_source, long include_depth) {
                ShadercIncludeResult res = ShadercIncludeResult.callocStack(stack);
                String src = filename.substring(0, filename.lastIndexOf('/')) + "/" + MemoryUtil.memUTF8(requested_source);
                try {
                    res.content(stack.bytes(Files.readAllBytes(Paths.get(src))));
                    res.source_name(MemoryUtil.memUTF8(src));
                    return res.address();
                } catch (IOException e) {
                    throw new AssertionError("Failed to resolve include: " + src);
                }
            }
        }, releaser = new ShadercIncludeResultRelease() {
            @Override
            public void invoke(long user_data, long include_result) {
                ShadercIncludeResult result = ShadercIncludeResult.create(include_result);
                MemoryUtil.memFree(result.source_name());
                result.free();
            }
        }, 0);

        long res = shaderc_compile_into_spv(compiler, bytes, vulkanStageToShadercKind(vulkanStage), stack.UTF8(filename), stack.UTF8("main"), options);
        if (res == 0) {
            throw new IllegalArgumentException("Failed to compile shader (internal error).");
        }
        if (shaderc_result_get_compilation_status(res) != shaderc_compilation_status_success) {
            throw new IllegalArgumentException("Failed to compile shader: " + shaderc_result_get_error_message(res));
        }

        int size = (int)shaderc_result_get_length(res);
        ByteBuffer result = stack.calloc(size);
        result.put(shaderc_result_get_bytes(res));
        result.flip();
        shaderc_result_release(res);
        shaderc_compiler_release(compiler);
        releaser.free();
        resolver.free();

        return result;
    }

    public ShaderManager(Context context) {
        this.ctx = context;
        this.shaderModules = new ArrayList<>();
    }

    public void cleanup() {
        while (!this.shaderModules.isEmpty()) {
            destroyShaderModule(this.shaderModules.get(0));
        }
    }

    public long createShaderModule(ByteBuffer code) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkShaderModuleCreateInfo info = VkShaderModuleCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                    .pCode(code)
                    .flags(0);
            LongBuffer lp = stack.mallocLong(1);
            int err = vkCreateShaderModule(this.ctx.getDevice(), info, null, lp);
            if (err != VK_SUCCESS) {
                throw new IllegalStateException("Failed to create shader module.");
            }
            this.shaderModules.add(lp.get(0));
            return lp.get(0);
        }
    }

    public long createShaderModule(String glslFilename, int shaderType) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer code = glslToSpirv(glslFilename, shaderType, stack);
            return createShaderModule(code);
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Failed to load glsl shader.");
        }
    }

    public void destroyShaderModule(long module) {
        if (!this.shaderModules.contains(module)) {
            throw new IllegalStateException("Failed to destroy missing shader module.");
        }
        this.shaderModules.remove(module);
        vkDestroyShaderModule(this.ctx.getDevice(), module, null);
    }
}