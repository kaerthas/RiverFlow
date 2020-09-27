/*
 *  *    Copyright (c) 2019-2025, Jason All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  *
 *  * Redistributions of source code must retain the above copyright notice,
 *  * this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *  * notice, this list of conditions and the following disclaimer in the
 *  * documentation and/or other materials provided with the distribution.
 *  * Neither the name of the yunho.top developer nor the names of its
 *  * contributors may be used to endorse or promote products derived from
 *  * this software without specific prior written permission.
 *  * Author: Jason (wujiang_job@163.com)
 */

package com.inspur.workinfo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jason
 * @date 2019-05-16
 * <p>
 * 字典类型
 */
@Getter
@AllArgsConstructor
public enum ProjectStateType {

	unsub("1", "草稿"),
	subed("2", "收件"),
	preaccepted("3", "预受理"),
	preacceptedback("4", "预受理退回"),
	accepted("5", "受理"),
	subcorrected("6", "补正补齐"),
	unaccepted("7", "不予受理"),
	doing("8", "在办"),
	dospecilup("9", "挂起"),
	done("10", "办结"),
	turndone("11", "转报办结"),
	baddone("12", "作废办结"),
	backdone("13", "退件");

	private String value;
	private String description;
}
