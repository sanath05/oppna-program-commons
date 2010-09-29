/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */

package se.vgregion.portal.core.infrastructure.persistence;

import java.io.Serializable;

import se.vgregion.portal.core.domain.patterns.entity.Entity;
import se.vgregion.portal.core.domain.patterns.repository.Repository;

/**
 * An extended repository interface with additional methods for find and remove {@link Entity entities} by its
 * database primary key.
 * 
 * @param <T>
 *            The Entity Type
 * @param <ID>
 *            The ID of the Entity
 * @param <PK>
 *            The type of the primary key
 * 
 * @author Anders Asplund - <a href="http://www.callistaenterprise.se">Callista Enterprise</a>
 */
public interface DatabaseRepository<T extends Entity<T, ID>, ID extends Serializable, PK extends Serializable>
        extends Repository<T, ID> {

    /**
     * Finds the instance of <code>T</code> identified by the database primary key. The primary key and the ID of
     * the entity is not necessarily the same. I.e the ID of a person could be it's social security number, while
     * the primary key is normally generated by the database.
     * 
     * @param pk
     *            The primary key
     * @return an object of <code>T</code>
     */
    T findByPrimaryKey(PK pk);

    /**
     * Remove entity by the database primary key. The primary key and the ID of the entity is not necessarily the
     * same. I.e the ID of a person could be it's social security number, while the primary key is normally
     * generated by the database.
     * 
     * @param pk
     *            The primary key
     * 
     */
    void removeByPrimaryKey(PK pk);
}
