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
 * This interface describes the methods used to write the messages from the
 * client to the gui. All methods must be thread safe.
 */
public interface App {

    /**
     * write a message to a log
     *
     * @param msg message to write
     */
    public void writeLog(String msg);

    /**
     * write an error message
     *
     * @param err error message to write
     */
    public void writeError(String err);

    /**
     * write a success message
     *
     * @param msg success message to write
     */
    public void writeSuccess(String msg);

    /**
     * create a notification for the user
     *
     * @param msg notification message
     */
    public void createAlarm(String msg);
}