package com.myorg.muleguard.checks;

import com.myorg.muleguard.model.Check;

import com.myorg.muleguard.checks.RULE_000_GenericCodeTokenCheck;
import com.myorg.muleguard.checks.RULE_001_PomParentCheck;
import com.myorg.muleguard.checks.RULE_002_PluginVersionCheck;
import com.myorg.muleguard.checks.RULE_003_PomPluginRemovedCheck;
import com.myorg.muleguard.checks.RULE_004_PomDependencyRemovedCheck;
import com.myorg.muleguard.checks.RULE_006_PomDependencyAddedCheck;
import com.myorg.muleguard.checks.RULE_005_ibmmqciphervaluecheck;
import com.myorg.muleguard.checks.RULE_007_MuleArtifactJsonCheck;
import com.myorg.muleguard.checks.RULE_008_apiautodiscoverycheck;
import com.myorg.muleguard.checks.RULE_009_UnsupportedErrorClassCheck;
import com.myorg.muleguard.checks.RULE_010_UnsupportedApplicationCodeCheck;
import com.myorg.muleguard.checks.RULE_011_DLPReferenceCheck;
import com.myorg.muleguard.checks.RULE_012_CryptoJceEncryptPbeCheck;
import com.myorg.muleguard.checks.RULE_013_CryptoJceConfigTypeCheck;
import com.myorg.muleguard.checks.RULE_014_tobase64TokenCheck;
import com.myorg.muleguard.checks.RULE_100_GenericConfigTokenCheck;
import com.myorg.muleguard.checks.RULE_101_ConfigPropertyCheck;
import com.myorg.muleguard.checks.RULE_102_ConfigPolicyCheck;


import java.util.HashMap;
import java.util.Map;

public class CheckFactory {
    private static Map<String, Class<? extends AbstractCheck>> registry = new HashMap<>();

    static {
        registry.put("GENERIC_CODE_TOKEN_CHECK", RULE_000_GenericCodeTokenCheck.class);
        registry.put("POM_PARENT", RULE_001_PomParentCheck.class);
        registry.put("POM_PROPERTY", RULE_002_PluginVersionCheck.class);
        registry.put("POM_PLUGIN_REMOVED", RULE_003_PomPluginRemovedCheck.class);
        registry.put("POM_DEPENDENCY_REMOVED", RULE_004_PomDependencyRemovedCheck.class);
        registry.put("IBM_MQ_CIPHER_CHECK", RULE_005_ibmmqciphervaluecheck.class);
        registry.put("POM_DEPENDENCY_ADDED", RULE_006_PomDependencyAddedCheck.class);
        registry.put("MULE_ARTIFACT_JSON_FULL", RULE_007_MuleArtifactJsonCheck.class);
        registry.put("XML_XPATH_EXISTS", RULE_008_apiautodiscoverycheck.class);
        registry.put("UNSUPPORTED_ERROR_EXPRESSIONS", RULE_009_UnsupportedErrorClassCheck.class);
        registry.put("UNSUPPORTED_XML_ATTRIBUTE", RULE_010_UnsupportedApplicationCodeCheck.class);
        registry.put("DLP_REFERENCE_CHECK", RULE_011_DLPReferenceCheck.class);
        registry.put("CRYPTO_JCE_ENCRYPT_PBE_CHECK", RULE_012_CryptoJceEncryptPbeCheck.class);
        registry.put("CRYPTO_JCE_CONFIG_TYPE_CHECK", RULE_013_CryptoJceConfigTypeCheck.class);
        registry.put("FORBIDDEN_TOKEN_IN_ELEMENT", RULE_014_tobase64TokenCheck.class);

        // Config Rules (starting from 100)
        registry.put("GENERIC_CONFIG_TOKEN_CHECK", RULE_100_GenericConfigTokenCheck.class);
        registry.put("SUBSTRING_TOKEN_CHECK", RULE_100_GenericConfigTokenCheck.class);
        registry.put("CONFIG_PROPERTY_EXISTS", RULE_101_ConfigPropertyCheck.class);
        registry.put("CONFIG_POLICY_EXISTS", RULE_102_ConfigPolicyCheck.class);
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