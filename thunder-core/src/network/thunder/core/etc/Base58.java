/*
 * ThunderNetwork - Server Client Architecture to send Off-Chain Bitcoin Payments
 * Copyright (C) 2015 Mats Jerratsch <matsjj@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package network.thunder.core.etc;

// TODO: Auto-generated Javadoc

/**
 * The Class Base58.
 */
public class Base58 {

	/**
	 * The Constant ALPHABET.
	 */
	private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();

	/**
	 * The Constant BASE_58.
	 */
	private static final int BASE_58 = ALPHABET.length;

	/**
	 * The Constant BASE_256.
	 */
	private static final int BASE_256 = 256;

	/**
	 * The Constant INDEXES.
	 */
	private static final int[] INDEXES = new int[128];

	static {
		for (int i = 0; i < INDEXES.length; i++) {
			INDEXES[i] = -1;
		}
		for (int i = 0; i < ALPHABET.length; i++) {
			INDEXES[ALPHABET[i]] = i;
		}
	}

	/**
	 * Copy of range.
	 *
	 * @param source the source
	 * @param from   the from
	 * @param to     the to
	 * @return the byte[]
	 */
	private static byte[] copyOfRange (byte[] source, int from, int to) {
		byte[] range = new byte[to - from];
		System.arraycopy(source, from, range, 0, range.length);

		return range;
	}

	/**
	 * Decode.
	 *
	 * @param input the input
	 * @return the byte[]
	 */
	public static byte[] decode (String input) {
		if (input.length() == 0) {
			// paying with the same coin
			return new byte[0];
		}

		byte[] input58 = new byte[input.length()];
		//
		// Transform the String to a base58 byte sequence
		//
		for (int i = 0; i < input.length(); ++i) {
			char c = input.charAt(i);

			int digit58 = -1;
			if (c >= 0 && c < 128) {
				digit58 = INDEXES[c];
			}
			if (digit58 < 0) {
				throw new RuntimeException("Not a Base58 input: " + input);
			}

			input58[i] = (byte) digit58;
		}

		//
		// Count leading zeroes
		//
		int zeroCount = 0;
		while (zeroCount < input58.length && input58[zeroCount] == 0) {
			++zeroCount;
		}

		//
		// The encoding
		//
		byte[] temp = new byte[input.length()];
		int j = temp.length;

		int startAt = zeroCount;
		while (startAt < input58.length) {
			byte mod = divmod256(input58, startAt);
			if (input58[startAt] == 0) {
				++startAt;
			}

			temp[--j] = mod;
		}

		//
		// Do no add extra leading zeroes, move j to first non null byte.
		//
		while (j < temp.length && temp[j] == 0) {
			++j;
		}

		return copyOfRange(temp, j - zeroCount, temp.length);
	}

	/**
	 * Divmod256.
	 *
	 * @param number58 the number58
	 * @param startAt  the start at
	 * @return the byte
	 */
	private static byte divmod256 (byte[] number58, int startAt) {
		int remainder = 0;
		for (int i = startAt; i < number58.length; i++) {
			int digit58 = number58[i] & 0xFF;
			int temp = remainder * BASE_58 + digit58;

			number58[i] = (byte) (temp / BASE_256);

			remainder = temp % BASE_256;
		}

		return (byte) remainder;
	}

	/**
	 * Divmod58.
	 *
	 * @param number  the number
	 * @param startAt the start at
	 * @return the byte
	 */
	private static byte divmod58 (byte[] number, int startAt) {
		int remainder = 0;
		for (int i = startAt; i < number.length; i++) {
			int digit256 = number[i] & 0xFF;
			int temp = remainder * BASE_256 + digit256;

			number[i] = (byte) (temp / BASE_58);

			remainder = temp % BASE_58;
		}

		return (byte) remainder;
	}

	/**
	 * Encode.
	 *
	 * @param input the input
	 * @return the string
	 */
	public static String encode (byte[] input) {
		if (input.length == 0) {
			// paying with the same coin
			return "";
		}

		//
		// Make a copy of the input since we are going to modify it.
		//
		input = copyOfRange(input, 0, input.length);

		//
		// Count leading zeroes
		//
		int zeroCount = 0;
		while (zeroCount < input.length && input[zeroCount] == 0) {
			++zeroCount;
		}

		//
		// The actual encoding
		//
		byte[] temp = new byte[input.length * 2];
		int j = temp.length;

		int startAt = zeroCount;
		while (startAt < input.length) {
			byte mod = divmod58(input, startAt);
			if (input[startAt] == 0) {
				++startAt;
			}

			temp[--j] = (byte) ALPHABET[mod];
		}

		//
		// Strip extra '1' if any
		//
		while (j < temp.length && temp[j] == ALPHABET[0]) {
			++j;
		}

		//
		// Add as many leading '1' as there were leading zeros.
		//
		while (--zeroCount >= 0) {
			temp[--j] = (byte) ALPHABET[0];
		}

		byte[] output = copyOfRange(temp, j, temp.length);
		return new String(output);
	}
}