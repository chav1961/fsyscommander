package chav1961.fsyscommander.settings;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

@LocaleResourceLocation("i18n:xml:root://chav1961.fsyscommander.settings.ViewAndEditorType/chav1961/fsyscommander/i18n/i18n.xml")
public enum ViewAndEditorType {
	@LocaleResource(value="chav1961.fsyscommander.settings.viewAndEditorType.internal",tooltip="chav1961.fsyscommander.settings.viewAndEditorType.internal.tt")
	INTERNAL,
	@LocaleResource(value="chav1961.fsyscommander.settings.viewAndEditorType.external",tooltip="chav1961.fsyscommander.settings.viewAndEditorType.external.tt")
	EXTERNAL;
}
