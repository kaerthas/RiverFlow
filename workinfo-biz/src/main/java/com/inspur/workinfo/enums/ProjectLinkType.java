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
public enum ProjectLinkType {

	accepted("1", "受理中"),
	done("2", "已办结"),
	canceled("3", "已撤销"),
	submited("4", "已提交");

	private String value;
	private String description;
}
