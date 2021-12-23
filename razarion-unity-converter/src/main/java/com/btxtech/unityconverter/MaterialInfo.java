package com.btxtech.unityconverter;

public class MaterialInfo {
    private GuidFile mainTexture;
    private GuidFile main2Texture;

    public GuidFile getMainTexture() {
        return mainTexture;
    }

    public void setMainTexture(GuidFile mainTexture) {
        this.mainTexture = mainTexture;
    }

    public GuidFile getMain2Texture() {
        return main2Texture;
    }

    public void setMain2Texture(GuidFile main2Texture) {
        this.main2Texture = main2Texture;
    }

    public MaterialInfo mainTexture(GuidFile mainTextureGuid) {
        setMainTexture(mainTextureGuid);
        return this;
    }

    public MaterialInfo main2Texture(GuidFile main2TextureGuid) {
        setMain2Texture(main2TextureGuid);
        return this;
    }

    public static class GuidFile {
        private String guid;
        private String file;

        public String getGuid() {
            return guid;
        }

        public void setGuid(String guid) {
            this.guid = guid;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public GuidFile guid(String guid) {
            setGuid(guid);
            return this;
        }

        public GuidFile file(String file) {
            setFile(file);
            return this;
        }
    }
}
