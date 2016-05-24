package gr.uoa.di.thanos.botcraft.geometry;

/**
 * Class representing a 3D vector.
 * 
 * @author thanos
 */
public class Vector {
	/**
	 * The zero vector.
	 */
	public static final Vector ZERO = new Vector(0.0f, 0.0f, 0.0f);

	/**
	 * The unit vector along the x axis.
	 */
	public static final Vector I = new Vector(1.0f, 0.0f, 0.0f);

	/**
	 * The unit vector along the y axis.
	 */
	public static final Vector J = new Vector(0.0f, 1.0f, 0.0f);

	/**
	 * The unit vector along the z axis.
	 */
	public static final Vector K = new Vector(0.0f, 0.0f, 1.0f);
	private static final String FORMAT = "(%1$ ,12.3f, %2$ ,12.3f, %3$ ,12.3f)";

	private final float x;
	private final float y;
	private final float z;

	/**
	 * Construct a new 3D vector.
	 * 
	 * @param x
	 *            the x component of this vector
	 * @param y
	 *            the y component of this vector
	 * @param z
	 *            the z component of this vector
	 */
	public Vector(final float x, final float y, final float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Get the x component of this vector.
	 * 
	 * @return the x component of this vector
	 */
	public float getX() {
		return x;
	}

	/**
	 * Get the y component of this vector.
	 * 
	 * @return the y component of this vector
	 */
	public float getY() {
		return y;
	}

	/**
	 * Get the z component of this vector.
	 * 
	 * @return the z component of this vector
	 */
	public float getZ() {
		return z;
	}

	/**
	 * Multiply this vector by a scalar.
	 * 
	 * @param scalar
	 *            the scalar to multiply by
	 * @return a new vector representing the product of this vector by the given scalar
	 */
	public Vector multiply(final float scalar) {
		return new Vector(scalar * x, scalar * y, scalar * z);
	}

	/**
	 * Divide this vector by a scalar.
	 * 
	 * @param scalar
	 *            the scalar to divide by (must not be zero)
	 * @return a new vector representing the quotient of this vector by the given scalar
	 */
	public Vector divide(final float scalar) {
		if (scalar == 0.0f) {
			throw new IllegalArgumentException("Can not divide by zero");
		}
		return multiply(1.0f / scalar);
	}

	/**
	 * Add a vector to this vector.
	 * 
	 * @param vector
	 *            the vector to add to this
	 * @return a new vector representing the sum of the given vector to this vector
	 */
	public Vector add(final Vector vector) {
		return new Vector(x + vector.x, y + vector.y, z + vector.z);
	}

	/**
	 * Subtract a vector from this vector.
	 * 
	 * @param vector
	 *            the vector to subtract from this
	 * @return a new vector representing the difference of the given vector from this vector
	 */
	public Vector subtract(final Vector vector) {
		return add(vector.multiply(-1.0f));
	}

	/**
	 * Calculate the dot product of this vector by another vector.
	 * 
	 * @param vector
	 *            the vector to multiply by
	 * @return a new vector representing the dot product of this vector by the given vector
	 */
	public float dot(final Vector vector) {
		return x * vector.x + y * vector.y + z * vector.z;
	}

	/**
	 * Calculate the cross product of this vector by another vector.
	 * 
	 * @param vector
	 *            the vector to multiply by
	 * @return a new vector representing the cross product of this vector by the given vector
	 */
	public Vector cross(final Vector vector) {
		return new Vector(y * vector.z - z * vector.y, z * vector.x - x * vector.z, x * vector.y - y * vector.z);
	}

	/**
	 * Calculate the length of this vector.
	 * 
	 * @return the length of this vector
	 */
	public float length() {
		return (float) Math.sqrt(dot(this));
	}

	/**
	 * Normalize this vector (must not have zero length).
	 * 
	 * @return a new vector representing the normal vector of this vector
	 */
	public Vector normalize() {
		final float length = length();
		if (length == 0.0f) {
			throw new IllegalStateException("Can not normalize a vector with zero length");
		}
		return divide(length);
	}

	@Override
	public boolean equals(final Object object) {
		return (object instanceof Vector) && (x == ((Vector) object).x) && (y == ((Vector) object).y) && (z == ((Vector) object).z);
	}

	@Override
	public int hashCode() {
		return Float.floatToIntBits(x) + Float.floatToIntBits(y) + Float.floatToIntBits(z);
	}

	@Override
	public String toString() {
		return String.format(FORMAT, x, y, z);
	}
}
