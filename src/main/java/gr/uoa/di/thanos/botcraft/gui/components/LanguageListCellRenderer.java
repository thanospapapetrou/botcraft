package gr.uoa.di.thanos.botcraft.gui.components;

import java.util.Locale;
import java.util.Objects;

/**
 * Simple list cell renderer for languages.
 * 
 * @author thanos
 */
public class LanguageListCellRenderer extends SimpleListCellRenderer<Locale> {
	@Override
	protected String item2String(final Locale language) {
		Objects.requireNonNull(language, "Language must not be null");
		return language.getDisplayName(language);
	}
}
