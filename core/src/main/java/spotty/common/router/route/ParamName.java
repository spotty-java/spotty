/*
 * Copyright 2022 - Alex Danilenko
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spotty.common.router.route;

import java.util.Objects;

import static spotty.common.validation.Validation.notBlank;

public final class ParamName {
    public final String name;
    public final String groupName; // regex group name for parser

    public ParamName(String name) {
        this.name = notBlank("name", name).replace(":", "");
        this.groupName = normalizeGroupName(name);
    }

    private String normalizeGroupName(String name) {
        return name.replaceAll("[^0-9a-zA-Z]", "");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParamName paramName = (ParamName) o;
        return Objects.equals(name, paramName.name)
            && Objects.equals(groupName, paramName.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, groupName);
    }
}
