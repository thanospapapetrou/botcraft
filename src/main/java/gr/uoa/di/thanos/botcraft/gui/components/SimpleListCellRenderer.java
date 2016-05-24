package gr.uoa.di.thanos.botcraft.gui.components;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Simple implementation of list cell renderer that renders an item as a string label.
 * 
 * @author thanos
 * @param <T>
 *            the item type
 */
public abstract class SimpleListCellRenderer<T> implements ListCellRenderer<T> {
	private final DefaultListCellRenderer defaultListCellRenderer;

	/**
	 * Construct a new simple list cell renderer.
	 */
	public SimpleListCellRenderer() {
		defaultListCellRenderer = new DefaultListCellRenderer();
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends T> items, T item, int index, boolean selected, boolean focused) {
		return defaultListCellRenderer.getListCellRendererComponent(items, item2String(item), index, selected, focused);
	}

	/**
	 * Convert an item to its corresponding string representation.
	 * 
	 * @param item
	 *            the item to convert
	 * @return the corresponding string representation of the given item.
	 */
	protected abstract String item2String(final T item);
}
