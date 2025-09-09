package com.dremio.throne.validate;

import java.util.Set;

public class PlayerNameMatcher {

    private static final Set<String> GAIA = Set.of("gaiaaa", "gaiaa", "gaaiaaa");
    private static final Set<String> REQUIEM = Set.of("requrem", "requzem");
    private static final Set<String> ELYEAT = Set.of("elveat");
    private static final Set<String> PRAD = Set.of("xpradel");
    private static final Set<String> FXT = Set.of("fxt1", "fxti", "fxtl", "exti");

    public static String match(String name) {
        String lowercase = name.toLowerCase();
        if (GAIA.contains(lowercase)) {
            return "Gaaiaa";
        }
        if (REQUIEM.contains(lowercase)) {
            return "Requiem";
        }
        if (ELYEAT.contains(lowercase)) {
            return "Elyeat";
        }
        if (PRAD.contains(lowercase)) {
            return "Pradel";
        }
        if (FXT.contains(lowercase)) {
            return "FxT1";
        }
        return name;
    }


}
