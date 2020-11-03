/*
 * Copyright (C) 2020 DeathsGun
 * deathsgun@protonmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package xyz.deathsgun.charon.service.workers;

import xyz.deathsgun.charon.model.Mod;
import xyz.deathsgun.charon.service.CharonActionCallback;
import xyz.deathsgun.charon.service.CharonService;
import xyz.deathsgun.charon.service.ProcessingType;
import xyz.deathsgun.charon.utils.ModUtils;

public class UpdateThread extends Thread {

    private final CharonService service;
    private final Mod mod;
    private final CharonActionCallback callback;

    public UpdateThread(CharonService service, CharonActionCallback callback, Mod mod) {
        this.service = service;
        this.callback = callback;
        this.mod = mod;
        this.setName("Updating " + mod.name);
        start();
    }

    @Override
    public void run() {
        ModUtils.deleteMod(mod); // TODO Error handling
        // TODO Install new mod
        service.removeProcess(mod);
        callback.onFinished(mod, ProcessingType.REMOVE);
        // TODO Give user notice to restart minecraft
    }
}
