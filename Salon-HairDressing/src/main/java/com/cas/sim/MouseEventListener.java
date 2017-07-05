package com.cas.sim;

import com.jme3.export.Savable;

public interface MouseEventListener extends Savable {
	/**
	 * Invoked when a mouse button has been pressed on a Node.
	 */
	public void mousePressed(MouseEvent e);

	/**
	 * Invoked when a mouse button has been released on a Node.
	 */
	public void mouseReleased(MouseEvent e);

	/**
	 * Invoked when the mouse button has been clicked (pressed and released) on a Node.
	 */
	public void mouseClicked(MouseEvent e);

	/**
	 * Invoked when the mouse enters a Node.
	 */
	public void mouseEntered(MouseEvent e);

	/**
	 * Invoked when the mouse exits a Node.
	 */
	public void mouseExited(MouseEvent e);

//	/**
//	 * Invoked when the mouse move a Node.
//	 */
//	public void mouseMove(MouseEvent e);
//
//	/**
//	 * Invoked when the mouse drag a Node.
//	 */
//	public void mouseDrag(MouseEvent e);
//
//	/**
//	 * Invoked when the mouse wheel a Node.
//	 */
//	public void mouseWheel(MouseEvent e);
//
//	/**
//	 * @param e
//	 */
//	public void update(MouseEvent e);
}
