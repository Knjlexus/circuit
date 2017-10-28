/**
 * 
 */
package com.cas.circuit;

import com.cas.circuit.consts.ServoConst;

/**
 * @author 张振宇 2015年10月9日 下午3:28:16
 */
public final class PulseLong implements Comparable<PulseLong> {
	private int resolution;
	private long boutCount; // a bout count 一转信号个数
	private int minorCount;

	/**
	 * 
	 */
	public PulseLong() {
		this(ServoConst.RESOLUTION);
	}

	/**
	 * 
	 */
	public PulseLong(int resolution) {
		this.resolution = resolution;
	}

	public final void addPulse() {
		minorCount++;
		if (minorCount == resolution) {
			boutCount++;
			minorCount = 0;
		}
	}

	/**
	 * 
	 */
	public final void confirm() {
//		System.out.println("PulseLong.confirm(boutCount" + boutCount + ", minorCount" + minorCount + ")");
	}

	/**
	 * @return the aBoutCount
	 */
	public long getBoutCount() {
		return boutCount;
	}

	/**
	 * @param boutCount the aBoutCount to set
	 */
	public void setBoutCount(long boutCount) {
		this.boutCount = boutCount;
	}

	/**
	 * @return the minorCount
	 */
	public int getMinorCount() {
		return minorCount;
	}

	/**
	 * @param minorCount the minorCount to set
	 */
	public void setMinorCount(int minorCount) {
		this.minorCount = minorCount;
	}

	/**
	 * @return the resolution
	 */
	public int getResolution() {
		return resolution;
	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		return super.hashCode();
////		final int prime = 31;
////		int result = 1;
////		result = prime * result + (int) (boutCount ^ (boutCount >>> 32));
////		result = prime * result + minorCount;
////		result = prime * result + resolution;
////		return result;
//	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PulseLong)) {
			return false;
		}
		PulseLong other = (PulseLong) obj;
		if (boutCount != other.boutCount) {
			return false;
		}
		if (minorCount != other.minorCount) {
			return false;
		}
		if (resolution != other.resolution) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(PulseLong obj) {
		if (obj == null) {
			return 1;
		}
		if (resolution != obj.resolution) {
			throw new RuntimeException("无法比较, 分辨率不统一");
		}
		if (boutCount > obj.boutCount) {
			return 1;
		}
		if (boutCount < obj.boutCount) {
			return -1;
		}
		if (minorCount > obj.minorCount) {
			return 1;
		}
		if (minorCount < obj.minorCount) {
			return -1;
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "hashCode:" + hashCode() + "[圈数=" + boutCount + ", 剩脉冲数=" + minorCount + "]";
	}
}
