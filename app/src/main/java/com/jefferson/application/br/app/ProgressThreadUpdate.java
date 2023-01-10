/*
 * Copyright (C) 2023 Jefferson Calheiros


 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.jefferson.application.br.app;

import android.os.*;
import android.util.*;
import com.jefferson.application.br.*;
import com.jefferson.application.br.util.*;

public class ProgressThreadUpdate extends Thread {

	private SimpleDialog dialog;
	private long max;
	private FileTransfer mTransfer;
	private boolean running = true;
	private String base = "";
	private String title = "";
	private int suffix = 0;
	private Handler mHandler = new Handler();
	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
	
			dialog.setTitle(title);
			if(suffix++ < 6) {
				title += ".";
			} else {
				title = base;
				suffix = 0;
			}
			mHandler.postDelayed(mRunnable, 250);
		}
	};
    
	public ProgressThreadUpdate(FileTransfer mTransfer, SimpleDialog dialog) {
		this.dialog = dialog;
		this.mTransfer = mTransfer;
		this.base = dialog.getContext().getString(R.string.movendo);
		this.title = base;
	}

	public void setMax(long max) {
		this.max = max;
	}
    
    public void destroy() {
		mHandler.removeCallbacks(mRunnable);
		running = false;
		
        try {
			join();
		} catch (InterruptedException e) {
            
        }
	}
    
	@Override
	public void start() {
		
		if(max == 0) {
			Log.w(getClass().getName(),"Max progress can't be zero! please use setMax() before start()");
		} else {
			super.start();
			mHandler.postDelayed(mRunnable, 250);
		}
	}
    
	
    
	@Override
	public void run() {
		while (running) {
			try {
                sleep(50);
            } catch (InterruptedException e) {
                
            }
			double kilobytes = mTransfer.getTransferredKilobytes();
			long progress = Math.round(((100 / (double)max) * kilobytes));
			dialog.setProgress((int)progress);
		}
		Log.i("UpdateThread", "Thread has finalized.");
	}
}
