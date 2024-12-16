package finalCampaign.graphics;

public class shaders {
    public static crosshairShader crosshair;

    public static void load() {
        crosshair = new crosshairShader();
    }

    public static class crosshairShader extends shader {
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
