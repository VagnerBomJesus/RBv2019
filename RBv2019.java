import com.ridgesoft.intellibrain.IntelliBrain;
import com.ridgesoft.robotics.Motor;
import com.ridgesoft.robotics.RangeFinder;
import com.ridgesoft.robotics.sensors.ParallaxPing;

public class RBv2019 {

	// ================================================================================
	// Constants
	// ================================================================================

	private static final int WAIT = 0; // Wait for the Start button
	private static final int NAVIGATE_RIGHT = 1; // Navigate by following the right wall.
	private static final int CENTER = 2; // Center the robot in relation to the candle flame.
	private static final int PUT_OUT = 3; // Put out the candle flame.

	private static final int BASE_POWER = 8; // Base power to move.
	private static final int ROTATE_POWER = 5; // Base power to rotate.

	private static final float ANGLE_TO_TIME_FACTOR = 10.5f; // Factor used by the rotateAngle()

	private static final int MIN_DISTANCE_FRONT = 15; // Minimum distance to front wall.
	private static final int MIN_DISTANCE_RIGHT = 15; // Minimum distance to right wall.

	private static final float GAIN = 0.9f; // Gain of the proportional control.
	private static final int DELTA_LIMITE = 5; // Delta limit of the proportional control.

	// ================================================================================
	// Objects and other variables.
	// ================================================================================

	private static Motor mLeftMotor;
	private static Motor mRightMotor;

	private static RangeFinder mFrontSonar;
	private static RangeFinder mRightSonar;

	public static void main(String[] args) {

		// ================================================================================
		// Creation of the objects.
		// ================================================================================

		mLeftMotor = new ContinuousRotationServo(IntelliBrain.getServo(1), false, 14);
		mRightMotor = new ContinuousRotationServo(IntelliBrain.getServo(2), true, 14);

		mFrontSonar = new ParallaxPing(IntelliBrain.getDigitalIO(4));
		mRightSonar = new ParallaxPing(IntelliBrain.getDigitalIO(5));

		// ================================================================================
		// State Machine.
		// ================================================================================
		int state = NAVIGATE_RIGHT;
		while (true) {
			switch (state) {
			case WAIT:
				break;
			case NAVIGATE_RIGHT:
				navigateRightState();
				break;
			case CENTER:
				break;
			case PUT_OUT:
				break;
			}
		}
	}

	// ================================================================================
	// Methods to implement the states.
	// ================================================================================

	private static int navigateRightState() {
		// ===== Action of the state =====
		// Rotate left if wall in front.
		if (getDistance(mFrontSonar) < MIN_DISTANCE_FRONT)
			rotateAngle(90);

		// Proportional control
		//float error = (getDistance(mRightSonar) - MIN_DISTANCE_RIGHT);
		//int delta = (int) (error * GAIN);
		
		int delta = (int) ((getDistance(mRightSonar) - MIN_DISTANCE_RIGHT) * GAIN);
		delta = (delta > DELTA_LIMITE ? DELTA_LIMITE : delta);
		move(BASE_POWER, delta);

		// ===== Transition conditions of the state =====

		return NAVIGATE_RIGHT;
	}

	// ================================================================================
	// Methods to read sensors.
	// ================================================================================

	private static float getDistance(RangeFinder s) {
		s.ping();
		wait(10);
		float d = s.getDistanceCm();
		return (d < 0 ? 100f : d);
	}

	// ================================================================================
	// Methods to move the robot.
	// ================================================================================

	private static void move(int power, int delta) {
		mLeftMotor.setPower(power + delta);
		mRightMotor.setPower(power - delta);
	}

	private static void rotate(int power) {
		mLeftMotor.setPower(-power);
		mRightMotor.setPower(power);
	}

	private static void rotateAngle(int angle) {
		if (angle < 0) {
			angle = -angle;
			rotate(-ROTATE_POWER);
		} else {
			rotate(ROTATE_POWER);
		}

		wait((int) (angle * ANGLE_TO_TIME_FACTOR));
		stop();
	}

	private static void stop() {
		mLeftMotor.stop();
		mRightMotor.stop();
	}

	private static void brake() {
		mLeftMotor.brake();
		mRightMotor.brake();
	}

	// ================================================================================
	// Other Methods
	// ================================================================================

	private static void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
