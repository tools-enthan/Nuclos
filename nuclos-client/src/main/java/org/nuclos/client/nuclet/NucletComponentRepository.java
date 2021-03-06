//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.client.nuclet;

import java.util.ArrayList;
import java.util.List;

import org.nuclos.api.ui.DesktopItemFactory;
import org.nuclos.api.ui.LayoutComponentFactory;
import org.nuclos.api.ui.MenuItem;
import org.nuclos.api.ui.UserSettingsEditor;

// @Component
public class NucletComponentRepository {

	private List<MenuItem> menuItems = new ArrayList<MenuItem>();
	private List<DesktopItemFactory> desktopItemFactories = new ArrayList<DesktopItemFactory>();
	private List<LayoutComponentFactory> layoutComponentFactories = new ArrayList<LayoutComponentFactory>();
	private List<UserSettingsEditor> userSettingsEditors = new ArrayList<UserSettingsEditor>();
	
	public NucletComponentRepository() {
	}
	
	public void addMenuItem(MenuItem mi) {
		menuItems.add(mi);
	}
	
	public List<MenuItem> getMenuItems() {
		return new ArrayList<MenuItem>(menuItems);
	}
	
	public void addDesktopItemFactory(DesktopItemFactory di) {
		desktopItemFactories.add(di);
	}
	
	public List<DesktopItemFactory> getDesktopItemFactories() {
		return new ArrayList<DesktopItemFactory>(desktopItemFactories);
	}
	
	public void addLayoutComponentFactory(LayoutComponentFactory lc) {
		layoutComponentFactories.add(lc);
	}
	
	public List<LayoutComponentFactory> getLayoutComponentFactories() {
		return new ArrayList<LayoutComponentFactory>(layoutComponentFactories);
	}
	
	public void addUserSettingsEditor(UserSettingsEditor use) {
		userSettingsEditors.add(use);
	}
	
	public List<UserSettingsEditor> getUserSettingsEditors() {
		return new ArrayList<UserSettingsEditor>(userSettingsEditors);
	}
}
