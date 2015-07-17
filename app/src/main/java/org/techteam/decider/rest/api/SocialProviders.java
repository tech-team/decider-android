package org.techteam.decider.rest.api;

public class SocialProviders {
    public static final String SOCIAL_PATH = "social_login";

    public enum Provider {
        VK("vk");

        private String providerPath;

        Provider(String providerPath) {
            this.providerPath = providerPath;
        }

        public String getProviderPath() {
            return providerPath;
        }
    }

    public static String getProviderPath(Provider provider) {
        return ApiUI.resolveApiUrl(SOCIAL_PATH, provider.getProviderPath());
    }
}
