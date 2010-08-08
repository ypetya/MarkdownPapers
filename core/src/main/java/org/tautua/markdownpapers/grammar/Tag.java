/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tautua.markdownpapers.grammar;

/**
 * @author Larry Ruiz
 */
public class Tag extends SimpleNode {
    protected String name;

    public Tag(Parser p, int i) {
        super(p, i);
    }

    public Tag(int i) {
        super(i);
    }

    public String getName() {
        return name;
    }
}