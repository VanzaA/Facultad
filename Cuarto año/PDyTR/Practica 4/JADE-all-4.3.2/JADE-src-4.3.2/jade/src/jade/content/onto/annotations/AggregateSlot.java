/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.content.onto.annotations;

//#J2ME_EXCLUDE_FILE

import jade.content.schema.ObjectSchema;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Allows to specify in the ontological schema the <code>cardMin</code>
 * (minimum cardinality), <code>cardMax</code> (maximum cardinality) and
 * <code>type</code> (class of the contained elements) attributes of the aggregate slot.<br>
 * The annotation is to be applied to the getter method.
 *
 * @author Paolo Cancedda
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AggregateSlot {
	Class type() default Object.class;
	int cardMin() default 0;
	int cardMax() default ObjectSchema.UNLIMITED;
}
