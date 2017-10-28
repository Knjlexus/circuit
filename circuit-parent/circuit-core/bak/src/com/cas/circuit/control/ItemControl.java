package com.cas.circuit.control;

import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
/**
 * 工件、材料等需要与元器件互动的物体
 * @功能 ItemControl.java
 * @作者 Cwj
 * @创建日期 2016年8月19日
 * @修改人 Cwj
 */
public abstract class ItemControl extends AbstractControl{

	/**
	 * 当元器件改变工件、材料的坐标位置时（工件、材料模型attach在元器件模型中跟着一起移动，而active为false）,需要control做的一些操作
	 */
	protected abstract void onMoving();
	
	protected abstract void stopPiece(Spatial collision);
}
