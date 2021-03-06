/*
 *
 *  Copyright 2012-2014 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.dom.asset;

import java.util.List;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.annotation.ActionSemantics.Of;

import org.estatio.dom.EstatioDomainService;
import org.estatio.dom.RegexValidation;
import org.estatio.dom.geography.Country;
import org.estatio.dom.utils.StringUtils;

@DomainService(repositoryFor = Property.class)
@DomainServiceLayout(
        named="Fixed Assets",
        menuBar = DomainServiceLayout.MenuBar.PRIMARY,
        menuOrder = "10.1"
)
public class Properties extends EstatioDomainService<Property> {

    public Properties() {
        super(Properties.class, Property.class);
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.NON_IDEMPOTENT)
    @MemberOrder(sequence = "1")
    public Property newProperty(
            final @Named("Reference") @RegEx(validation = RegexValidation.Property.REFERENCE, caseSensitive = true) String reference,
            final @Named("Name") String name,
            final PropertyType propertyType,
            final @Named("City") @Optional String city,
            final @Optional Country country,
            final @Named("Acquire date") @Optional LocalDate acquireDate) {
        final Property property = newTransientInstance();

        property.setReference(reference);
        property.setName(name);
        property.setType(propertyType);

        property.setCity(city);
        property.setCountry(country);
        property.setAcquireDate(acquireDate);

        if (city != null && country != null && property.getLocation() == null) {
            property.lookupLocation(city.concat(", ").concat(country.getName()));
        }

        persistIfNotAlready(property);
        return property;
    }

    public PropertyType default2NewProperty() {
        return PropertyType.MIXED;
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "2")
    public List<Property> findProperties(
            @Named("Reference or Name") final String referenceOrName) {
        return allMatches("findByReferenceOrName",
                "referenceOrName", StringUtils.wildcardToCaseInsensitiveRegex(referenceOrName));
    }

    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "3")
    public List<Property> allProperties() {
        return allInstances();
    }

    // //////////////////////////////////////

    @Programmatic
    public Property findPropertyByReference(final String reference) {
        return mustMatch("findByReference","reference", reference);
    }

    @Programmatic
    public Property findPropertyByReferenceElseNull(final String reference) {
        return firstMatch("findByReference","reference", reference);
    }

    // //////////////////////////////////////

    @Hidden
    public List<Property> autoComplete(final String searchPhrase) {
        return findProperties("*".concat(searchPhrase).concat("*"));
    }
}
