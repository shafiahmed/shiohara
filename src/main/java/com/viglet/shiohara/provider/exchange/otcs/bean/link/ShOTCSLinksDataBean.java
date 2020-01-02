/*
 * Copyright (C) 2016-2020 the original author or authors. 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.viglet.shiohara.provider.exchange.otcs.bean.link;

/**
 * @author Alexandre Oliveira
 * 
 * @since 0.3.6
 */
public class ShOTCSLinksDataBean {

	private ShOTCSLinksDataSelfBean self;
	
	private ShOTCSLinksDataSelfBean next;

	public ShOTCSLinksDataSelfBean getSelf() {
		return self;
	}

	public void setSelf(ShOTCSLinksDataSelfBean self) {
		this.self = self;
	}

	public ShOTCSLinksDataSelfBean getNext() {
		return next;
	}

	public void setNext(ShOTCSLinksDataSelfBean next) {
		this.next = next;
	}

}
