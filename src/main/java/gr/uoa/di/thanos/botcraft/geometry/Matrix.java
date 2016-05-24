package gr.uoa.di.thanos.botcraft.geometry;

import java.nio.FloatBuffer;

/**
 * Class representing a 4 x 4 matrix for transformations with homogenous coordinates.
 * 
 * @author thanos
 */
public class Matrix {
	/**
	 * The identity matrix.
	 */
	public static final Matrix IDENTITY = new Matrix();

	private static final String FORMAT = "\u23a1%1$ ,12.3f %2$ ,12.3f %3$ ,12.3f %4$ ,12.3f\u23a4\n\u23a2%5$ ,12.3f %6$ ,12.3f %7$ ,12.3f %8$ ,12.3f\u23a5\n\u23a2%9$ ,12.3f %10$ ,12.3f %11$ ,12.3f %12$ ,12.3f\u23a5\n\u23a3%13$ ,12.3f %14$ ,12.3f %15$ ,12.3f %16$ ,12.3f\u23a6";

	private final FloatBuffer buffer;

	static {
		for (final MatrixComponent element : MatrixComponent.values()) {
			IDENTITY.put(element, element, 1.0f);
		}
	}

	/**
	 * Calculate a translation matrix.
	 * 
	 * @param vector
	 *            the vector to translate by
	 * @return a matrix corresponding to translation by the given vector
	 */
	public static Matrix translation(final Vector vector) {
		final Matrix translation = new Matrix();
		for (final MatrixComponent element : MatrixComponent.values()) {
			translation.put(element, element, 1.0f);
		}
		translation.put(MatrixComponent.X, MatrixComponent.W, vector.getX());
		translation.put(MatrixComponent.Y, MatrixComponent.W, vector.getY());
		translation.put(MatrixComponent.Z, MatrixComponent.W, vector.getZ());
		return translation;
	}

	/**
	 * Calculate a rotation matrix.
	 * 
	 * @param angle
	 *            the angle to rotate by (must be in radians)
	 * @param vector
	 *            the vector specifying the direction around which to rotate (must not have zero length)
	 * @return a matrix corresponding to rotation by the given angle around the given vector
	 */
	public static Matrix rotation(final float angle, final Vector vector) {
		if (vector.length() == 0.0f) {
			throw new IllegalArgumentException("Vector must not have zero length");
		}
		final float sin = (float) Math.sin(angle);
		final float cos = (float) Math.cos(angle);
		final float dCos = 1.0f - cos;
		final Vector normal = vector.normalize();
		final float x = normal.getX();
		final float y = normal.getY();
		final float z = normal.getZ();
		final float xSin = x * sin;
		final float ySin = y * sin;
		final float zSin = z * sin;
		final float xYDCos = x * y * dCos;
		final float xZDCos = x * z * dCos;
		final float yZDCos = y * z * dCos;
		final Matrix rotation = new Matrix();
		rotation.put(MatrixComponent.X, MatrixComponent.X, x * x * dCos + cos);
		rotation.put(MatrixComponent.X, MatrixComponent.Y, xYDCos - zSin);
		rotation.put(MatrixComponent.X, MatrixComponent.Z, xZDCos + ySin);
		rotation.put(MatrixComponent.Y, MatrixComponent.X, xYDCos + zSin);
		rotation.put(MatrixComponent.Y, MatrixComponent.Y, y * y * dCos + cos);
		rotation.put(MatrixComponent.Y, MatrixComponent.Z, yZDCos - xSin);
		rotation.put(MatrixComponent.Z, MatrixComponent.X, xZDCos - ySin);
		rotation.put(MatrixComponent.Z, MatrixComponent.Y, yZDCos + xSin);
		rotation.put(MatrixComponent.Z, MatrixComponent.Z, z * z * dCos + cos);
		rotation.put(MatrixComponent.W, MatrixComponent.W, 1.0f);
		return rotation;
	}

	/**
	 * Calculate a perspective projection matrix.
	 * 
	 * @param left
	 *            position of the left plane of the perspective projection along the x axis
	 * @param right
	 *            the position of the right plane of the perspective projection along the x axis
	 * @param bottom
	 *            the position of the bottom plane of the perspective projection along the y axis
	 * @param top
	 *            the position of the top plane of the perspective projection along the y axis
	 * @param near
	 *            the position of the near plane of the perspective projection along the z axis
	 * @param far
	 *            the position of the far plane of the perspective projection along the z axis
	 * @return a matrix corresponding to perspective projection between the given planes
	 */
	public static Matrix perspectiveProjection(final float left, final float right, final float bottom, final float top, final float near, final float far) {
		if (left >= right) {
			throw new IllegalArgumentException("Left must be less than right");
		}
		if (bottom >= top) {
			throw new IllegalArgumentException("Bottom must be less than top");
		}
		if (near <= 0.0f) {
			throw new IllegalArgumentException("Near must be positive");
		}
		if (far <= 0.0f) {
			throw new IllegalArgumentException("Far  must be positive");
		}
		if (near >= far) {
			throw new IllegalArgumentException("Near must be less than far");
		}
		final float dX = right - left;
		final float dY = top - bottom;
		final float dZ = near - far;
		final Matrix perspectiveProjection = new Matrix();
		perspectiveProjection.put(MatrixComponent.X, MatrixComponent.X, 2.0f * near / dX);
		perspectiveProjection.put(MatrixComponent.X, MatrixComponent.Z, (right + left) / dX);
		perspectiveProjection.put(MatrixComponent.Y, MatrixComponent.Y, 2.0f * near / dY);
		perspectiveProjection.put(MatrixComponent.Y, MatrixComponent.Z, (top + bottom) / dY);
		perspectiveProjection.put(MatrixComponent.Z, MatrixComponent.Z, (near + far) / dZ);
		perspectiveProjection.put(MatrixComponent.Z, MatrixComponent.W, 2.0f * near * far / dZ);
		perspectiveProjection.put(MatrixComponent.W, MatrixComponent.Z, -1.0f);
		return perspectiveProjection;
	}

	/**
	 * Calculate a perspective projection matrix.
	 * 
	 * @param fieldOfViewX
	 *            the field of view angle along the x axis (must be in radians)
	 * @param fieldOfViewY
	 *            the field of view angle along the y axis (must be in radians)
	 * @param near
	 *            the position of the near plane of the perspective projection along the z axis
	 * @param far
	 *            the position of the far plane of the perspective projection along the z axis
	 * @return a matrix corresponding to perspective projection with the given field of view angles and between the given planes
	 */
	public static Matrix perspectiveProjection(final float fieldOfViewX, final float fieldOfViewY, final float near, final float far) {
		final float x = ((float) Math.tan(fieldOfViewX / 2.0f)) * near;
		final float y = ((float) Math.tan(fieldOfViewY / 2.0f)) * near;
		return perspectiveProjection(-x, x, -y, y, near, far);
	}

	private Matrix() {
		buffer = FloatBuffer.allocate(MatrixComponent.values().length * MatrixComponent.values().length);
	}

	/**
	 * Get the buffer containing the data of this matrix.
	 * 
	 * @return the buffer containing the data of this matrix
	 */
	public FloatBuffer getBuffer() {
		return buffer;
	}

	/**
	 * Multiply this matrix by another matrix.
	 * 
	 * @param matrix
	 *            the matrix to multiply this matrix by
	 * @return a new matrix corresponding to the product of this matrix by the given matrix
	 */
	public Matrix multiply(final Matrix matrix) {
		final Matrix product = new Matrix();
		for (final MatrixComponent i : MatrixComponent.values()) {
			for (final MatrixComponent j : MatrixComponent.values()) {
				float sum = 0.0f;
				for (final MatrixComponent k : MatrixComponent.values()) {
					sum += get(i, k) * matrix.get(k, j);
				}
				product.put(i, j, sum);
			}
		}
		return product;
	}

	@Override
	public boolean equals(final Object object) {
		if (!(object instanceof Matrix)) {
			return false;
		}
		final Matrix matrix = (Matrix) object;
		for (final MatrixComponent row : MatrixComponent.values()) {
			for (final MatrixComponent column : MatrixComponent.values()) {
				if (get(row, column) != matrix.get(row, column)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		for (final MatrixComponent row : MatrixComponent.values()) {
			for (final MatrixComponent column : MatrixComponent.values()) {
				hashCode += Float.floatToIntBits(get(row, column));
			}
		}
		return hashCode;
	}

	@Override
	public String toString() {
		final Object[] arguments = new Object[buffer.array().length]; // buffer.array() returns primitive array so boxing is needed
		for (int i = 0; i < buffer.array().length; i++) {
			arguments[i] = buffer.array()[i];
		}
		return String.format(FORMAT, arguments);
	}

	private float get(final MatrixComponent row, final MatrixComponent column) {
		return buffer.get(row.ordinal() * MatrixComponent.values().length + column.ordinal());
	}

	private void put(final MatrixComponent row, final MatrixComponent column, final float value) {
		buffer.put(row.ordinal() * MatrixComponent.values().length + column.ordinal(), value);
	}
}
