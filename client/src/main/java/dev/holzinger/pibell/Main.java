// Copyright (C) 2021 Paul Holzinger
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.

package dev.holzinger.pibell;

/**
 * Main class uses to launch the GUI. I do not know why the extra class is
 * needed but without this the jar will not work correctly.
 */
public class Main {
    public static void main(String[] args) {
        GUI.launch(GUI.class, args);
    }
}
