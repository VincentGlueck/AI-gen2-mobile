package org.ww.ai.rds.ifenum;

import androidx.room.TypeConverter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum RenderModel {

    STABLE_DIFFUSION_15("Stable diffusion 1.5"),
    STABLE_DIFFUSION_20("Stable diffusion 2.0"),
    SDXL_BETA("SDXL BETA"),
    SDXL_0_9("SDXL 0.9"),
    DALL_E_2("DALL-E 2"),
    STABLE_INP_1_0("Stable Inpainting 1.0"),
    STABLE_INP_2_0("Stable Inpainting 2.0");


    private final String name;

    RenderModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @TypeConverter
    public static RenderModel fromName(String name) {
        Optional<RenderModel> optional = Stream.of(RenderModel.values()).filter(rm -> rm.getName().equalsIgnoreCase(name)).findFirst();
        if(optional.isPresent()) {
            return optional.get();
        }
        throw new IllegalArgumentException("invalid RenderModel: " + name);
    }

    @TypeConverter
    public static String getName(RenderModel model) {
        return model.getName();
    }

    public static List<String> getAvailableModels() {
        return Stream.of(values()).map(rm -> rm.getName()).collect(Collectors.toList());
    }
}
