/* 
 *  Koffee - A simple collection of utilities I commonly use
 *  Copyright (C) 2019-2020 Justin Crawford <justin@Stacksmash.net>
 *  Copyright (C) 2019-2020 Zachery Coleman <Zachery@Stacksmash.net>
 *  Copyright (C) 2019-2020 Skye Elliot <actuallyori@gmail.com>
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ristexsoftware.koffee;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.ristexsoftware.koffee.util.ShortID;

import lombok.Getter;
import lombok.Setter;

/**
 * a thing that exists
 */
public class Koffee {
    @Getter
    public static Logger logger = Logger.getLogger("koffee");

    @Getter @Setter
    private static ExecutorService pool = Executors.newFixedThreadPool(3);
}