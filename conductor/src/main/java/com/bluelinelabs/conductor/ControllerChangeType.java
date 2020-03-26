/*
 * Copyright 2020 Lalafo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bluelinelabs.conductor;

/**
 * All possible types of {@link Controller} changes to be used in {@link ControllerChangeHandler}s
 */
public enum ControllerChangeType {
    /** The Controller is being pushed to the host container */
    PUSH_ENTER(true, true),

    /** The Controller is being pushed to the backstack as another Controller is pushed to the host container */
    PUSH_EXIT(true, false),

    /** The Controller is being popped from the backstack and placed in the host container as another Controller is popped */
    POP_ENTER(false, true),

    /** The Controller is being popped from the host container */
    POP_EXIT(false, false);

    public boolean isPush;
    public boolean isEnter;

    ControllerChangeType(boolean isPush, boolean isEnter) {
        this.isPush = isPush;
        this.isEnter = isEnter;
    }
}
