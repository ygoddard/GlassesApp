/*
 * This work contains files distributed in Android, such files Copyright (C) 2016 The Android Open Source Project
 *
 * and are Licensed under the Apache License, Version 2.0 (the "License"); you may not use these files except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
*/


package sdk.everysight.examples.gps;

import android.graphics.drawable.Drawable;

import com.everysight.common.carouselm.ItemInfo;

/**
 * Created by eran on 19/01/2017.
 */

public abstract class ItemInfoExe extends ItemInfo
{
    public ItemInfoExe(String itemName, Drawable itemDrawable)
    {
        super(itemName, itemDrawable);
    }

    public abstract void execute();
}
