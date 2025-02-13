package finalCampaign.graphics;

public class fcShaders {
    public static crosshairShader crosshair;

    public static void load() {
        crosshair = new crosshairShader();
    }

    public static class crosshairShader extends fcShader {
        public float a = 1f;

        public crosshairShader() {
            super("raw.default", "crosshair");
        }

        @Override
        public void apply() {
            setUniformf("color_a", a);
        }
    }
}
