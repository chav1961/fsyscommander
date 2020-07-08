package chav1961.fsyscommander.settings;

import java.awt.Color;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

@LocaleResourceLocation("i18n:xml:root://chav1961.fsyscommander.settings.ApplicationColorScheme/chav1961/fsyscommander/i18n/i18n.xml")
public enum ApplicationColorScheme {
	@LocaleResource(value="chav1961.fsyscommander.settings.applicationColorScheme.dark",tooltip="chav1961.fsyscommander.settings.applicationColorScheme.dark.tt")
	DARK(Color.BLACK,Color.DARK_GRAY,Color.GREEN),
	@LocaleResource(value="chav1961.fsyscommander.settings.applicationColorScheme.light",tooltip="chav1961.fsyscommander.settings.applicationColorScheme.light.tt")
	LIGHT(Color.WHITE,Color.DARK_GRAY,Color.BLACK);
	
	private final Color	bkColor, selectedBkColor, foreColor;
	
	ApplicationColorScheme(final Color bkColor, final Color selectedBkColor, final Color foreColor) {
		this.bkColor = bkColor;
		this.selectedBkColor = selectedBkColor;
		this.foreColor = foreColor;
	}
	
	public Color getBackGroundColor() {
		return bkColor;
	}

	public Color getSelectedBackGroundColor() {
		return selectedBkColor;
	}

	public Color getForeGroundColor() {
		return foreColor;
	}
}
