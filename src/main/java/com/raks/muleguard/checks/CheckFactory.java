package com.raks.muleguard.checks;

import com.raks.muleguard.model.Check;

import java.util.HashMap;
import java.util.Map;

public class CheckFactory {
    private static Map<String, Class<? extends AbstractCheck>> registry = new HashMap<>();

    static {
        // === GENERIC CONSOLIDATED CHECKS (New) ===
        // GenericTokenSearchCheck handles all token-based searches
        registry.put("GENERIC_TOKEN_SEARCH", GenericTokenSearchCheck.class);

        // GenericXmlValidationCheck handles all XML validation
        registry.put("GENERIC_XML_VALIDATION", GenericXmlValidationCheck.class);

        // GenericPropertyFileCheck handles all property file validation
        registry.put("GENERIC_PROPERTY_FILE", GenericPropertyFileCheck.class);

        // GenericPomValidationCheck handles all POM validation (dependencies, plugins,
        // properties)
        registry.put("GENERIC_POM_VALIDATION", GenericPomValidationCheck.class);

        // === LEGACY MAPPINGS (For backward compatibility) ===
        // Token search mappings
        registry.put("GENERIC_CODE_TOKEN_CHECK", GenericTokenSearchCheck.class);
        registry.put("DLP_REFERENCE_CHECK", GenericTokenSearchCheck.class);
        registry.put("FORBIDDEN_TOKEN_IN_ELEMENT", GenericTokenSearchCheck.class);
        registry.put("GENERIC_CONFIG_TOKEN_CHECK", GenericTokenSearchCheck.class);
        registry.put("SUBSTRING_TOKEN_CHECK", GenericTokenSearchCheck.class);

        // XML validation mappings
        registry.put("IBM_MQ_CIPHER_CHECK", GenericXmlValidationCheck.class);
        registry.put("XML_XPATH_EXISTS", GenericXmlValidationCheck.class);
        registry.put("UNSUPPORTED_XML_ATTRIBUTE", GenericXmlValidationCheck.class);
        registry.put("CRYPTO_JCE_ENCRYPT_PBE_CHECK", GenericXmlValidationCheck.class);
        registry.put("CRYPTO_JCE_CONFIG_TYPE_CHECK", GenericXmlValidationCheck.class);

        // Property file mappings
        registry.put("CONFIG_PROPERTY_EXISTS", GenericPropertyFileCheck.class);
        registry.put("CONFIG_POLICY_EXISTS", GenericPropertyFileCheck.class);

        // POM validation mappings (NEW - using GenericPomValidationCheck)
        registry.put("POM_PLUGIN_REMOVED", GenericPomValidationCheck.class);
        registry.put("POM_DEPENDENCY_REMOVED", GenericPomValidationCheck.class);
        registry.put("POM_DEPENDENCY_ADDED", GenericPomValidationCheck.class);

        // Custom validators with hardcoded patterns (to avoid YAML escaping issues)
        registry.put("CLIENTIDMAP_VALIDATOR", ClientIDMapCheck.class);

        // === SPECIFIC RULE IMPLEMENTATIONS (Keep for now) ===
        registry.put("POM_PARENT", RULE_001_PomParentCheck.class);
        registry.put("POM_PROPERTY", RULE_002_PluginVersionCheck.class);
        registry.put("MULE_ARTIFACT_JSON_FULL", RULE_007_MuleArtifactJsonCheck.class);
        registry.put("UNSUPPORTED_ERROR_EXPRESSIONS", RULE_009_UnsupportedErrorClassCheck.class);
    }

    public static AbstractCheck create(Check check) {
        try {
            Class<? extends AbstractCheck> clazz = registry.get(check.getType());
            if (clazz == null)
                throw new IllegalArgumentException("Unknown check type: " + check.getType());
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create check: " + check.getType(), e);
        }
    }
}