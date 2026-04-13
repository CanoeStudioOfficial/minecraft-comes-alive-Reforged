package mca.core;

import com.google.common.base.Charsets;
import net.minecraft.util.StringUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class Localizer {
    private Map<String, String> localizerMap = new HashMap<>();
    private static final ArrayList<String> EMPTY_LIST = new ArrayList<>();

    public Localizer() {
        InputStream inStream = null;
        try {

            String currentLangCode = MCA.proxy.getLanguageCode();

            String langFilePath = String.format("/assets/mca/lang/%s.lang", currentLangCode);
            inStream = Localizer.class.getResourceAsStream(langFilePath);

            if (inStream == null) {
                MCA.getLog().warn("Language file not found : " + langFilePath + "，Fall back to default language en_us.lang");
                inStream = Localizer.class.getResourceAsStream("/assets/mca/lang/en_us.lang");
            }

            if (inStream == null) {
                MCA.getLog().error("Language file not found : /assets/mca/lang/en_us.lang");
                return;
            }

            List<String> lines = IOUtils.readLines(inStream, Charsets.UTF_8);

            for (String line : lines) {
                if (line.startsWith("#") || line.isEmpty()) continue;
                String[] split = line.split("=", 2); // 避免值中出现 "=" 导致 split 数组越界
                if (split.length < 2) continue;
                localizerMap.put(split[0].trim(), split[1].trim());
            }

        } catch (IOException e) {
            MCA.getLog().error("Failed to initialize language file : " + e.getMessage());
        }
    }

    public String localize(String key, String... vars) {
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, vars);
        return localize(key, vars != null ? list : EMPTY_LIST);
    }

    public String localize(String key, ArrayList<String> vars) {
        String result = localizerMap.getOrDefault(key, key);
        if (result.equals(key)) {
            List<String> responses = localizerMap.entrySet().stream()
                    .filter(entry -> entry.getKey().contains(key))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
            if (!responses.isEmpty()) {
                result = responses.get(new Random().nextInt(responses.size()));
            }
        }
        return parseVars(result, vars).replaceAll("\\\\", "");
    }

    private String parseVars(String str, ArrayList<String> vars) {
        int index = 1;
        if (MCA.getInstance() != null) {
            str = str.replaceAll("%Supporter%", MCA.getInstance().getRandomSupporter());
        }

        // Handle %v1%, %v2%, etc.
        String varString = "%v" + index + "%";
        while (str.contains("%v") && index < 10) {
            try {
                str = str.replaceAll(varString, vars.get(index - 1));
            } catch (IndexOutOfBoundsException e) {
                str = str.replaceAll(varString, "");
                MCA.getLog().warn("Failed to substitute variables : " + varString + " in string : " + str);
            } finally {
                index++;
                varString = "%v" + index + "%";
            }
        }

        // Handle %s (standard Minecraft/Java format)
        if (str.contains("%s")) {
            for (String var : vars) {
                str = str.replaceFirst("%s", var);
            }
        }

        return str;
    }
}