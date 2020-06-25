package chav1961.fsyscommander.settings;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

@LocaleResourceLocation("i18n:xml:root://chav1961.fsyscommander.settings.ApplicationColorScheme/chav1961/fsyscommander/i18n/i18n.xml")
public enum ApplicationColorScheme {
	@LocaleResource(value="chav1961.fsyscommander.settings.applicationColorScheme.dark",tooltip="chav1961.fsyscommander.settings.applicationColorScheme.dark.tt")
	DARK,
	@LocaleResource(value="chav1961.fsyscommander.settings.applicationColorScheme.light",tooltip="chav1961.fsyscommander.settings.applicationColorScheme.light.tt")
	LIGHT
}
