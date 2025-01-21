package finalCampaign.util;

// code from webkit

public class bezier {
    public static final int CUBIC_BEZIER_SPLINE_SAMPLES = 11;
    public static final double kBezierEpsilon = 1e-7;
    public static final int kMaxNewtonIterations = 4;

    private double ax;
    private double bx;
    private double cx;
    
    private double ay;
    private double by;
    private double cy;

    private double dx;
    private double dy;
    
    private double sx;
    private double sy;

    private double startGradient;
    private double endGradient;

    private double[] splineSamples;

    public bezier() {
        splineSamples = new double[CUBIC_BEZIER_SPLINE_SAMPLES];
    }

    public bezier(double p0x, double p0y, double p1x, double p1y, double p2x, double p2y, double p3x, double p3y) {
        this();
        set(p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y);
    }

    public bezier set(double p0x, double p0y, double p1x, double p1y, double p2x, double p2y, double p3x, double p3y) {
        arrays.fillD(splineSamples, 0d);

        cx = 3.0 * p1x;
        bx = 3.0 * (p2x - p1x) - cx;
        ax = 1.0 - cx -bx;

        cy = 3.0 * p1y;
        by = 3.0 * (p2y - p1y) - cy;
        ay = 1.0 - cy - by;

        if (p1x > 0)
            startGradient = p1y / p1x;
        else if (p1y!=0 && p2x > 0)
            startGradient = p2y / p2x;
        else if (p1y!=0 && p2y!=0)
            startGradient = 1;
        else
            startGradient = 0;
        if (p2x < 1)
            endGradient = (p2y - 1) / (p2x - 1);
        else if (p2y == 1 && p1x < 1)
            endGradient = (p1y - 1) / (p1x - 1);
        else if (p2y == 1 && p1y == 1)
            endGradient = 1;
        else
            endGradient = 0;

        double deltaT = 1.0 / (CUBIC_BEZIER_SPLINE_SAMPLES - 1);
        for (int i = 0; i < CUBIC_BEZIER_SPLINE_SAMPLES; i++)
            splineSamples[i] = sampleCurveX(i * deltaT);


        dx = p0x;
        dy = p0y;

        sx = p3x - p0x;
        sy = p3y - p0y;

        return this;
    }

    private double sampleCurveX(double t) {
        // `ax t^3 + bx t^2 + cx t' expanded using Horner's rule.
        return ((ax * t + bx) * t + cx) * t;
    }
    
    private double sampleCurveY(double t) {
        return ((ay * t + by) * t + cy) * t;
    }
    
    private double sampleCurveDerivativeX(double t) {
        return (3.0 * ax * t + 2.0 * bx) * t + cx;
    }

    // Given an x value, find a parametric value it came from.
    private double solveCurveX(double x, double epsilon) {
        double t0 = 0.0;
        double t1 = 0.0;
        double t2 = x;
        double x2 = 0.0;
        double d2;
        int i;

        // Linear interpolation of spline curve for initial guess.
        double deltaT = 1.0 / (CUBIC_BEZIER_SPLINE_SAMPLES - 1);
        for (i = 1; i < CUBIC_BEZIER_SPLINE_SAMPLES; i++) {
            if (x <= splineSamples[i]) {
                t1 = deltaT * i;
                t0 = t1 - deltaT;
                t2 = t0 + (t1 - t0) * (x - splineSamples[i - 1]) / (splineSamples[i] - splineSamples[i - 1]);
                break;
            }
        }

        // Perform a few iterations of Newton's method -- normally very fast.
        // See https://en.wikipedia.org/wiki/Newton%27s_method.
        double newtonEpsilon = Math.min(kBezierEpsilon, epsilon);
        for (i = 0; i < kMaxNewtonIterations; i++) {
            x2 = sampleCurveX(t2) - x;
            if (Math.abs(x2) < newtonEpsilon)
                return t2;
            d2 = sampleCurveDerivativeX(t2);
            if (Math.abs(d2) < kBezierEpsilon)
                break;
            t2 = t2 - x2 / d2;
        }
        if (Math.abs(x2) < epsilon)
            return t2;

        // Fall back to the bisection method for reliability.
        while (t0 < t1) {
            x2 = sampleCurveX(t2);
            if (Math.abs(x2 - x) < epsilon)
                return t2;
            if (x > x2)
                t0 = t2;
            else
                t1 = t2;
            t2 = (t1 + t0) * .5;
        }

        // Failure.
        return t2;
    }

    public double solve(double x, double epsilon) {
        x -= dx;
        x /= sx;

        if (x < 0.0)
            return dy + (0.0 + startGradient * x) * sy;
        if (x > 1.0)
            return dy + (1.0 + endGradient * (x - 1.0)) * sy;
        return dy + sampleCurveY(solveCurveX(x, epsilon)) * sy;
    }

    public double solve(double x) {
        return solve(x, kBezierEpsilon);
    }

}
