/**
 * Copyright or © or Copr. Ministère Français chargé de la Culture
 * et de la Communication (2013)
 * <p/>
 * contact.gincoculture_at_gouv.fr
 * <p/>
 * This software is a computer program whose purpose is to provide a thesaurus
 * management solution.
 * <p/>
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software. You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * <p/>
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited liability.
 * <p/>
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systemsand/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 * <p/>
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */

package fr.mcc.ginco.solr;

import fr.mcc.ginco.beans.ThesaurusConcept;

import java.util.List;

public interface IConceptIndexerService {
	/**
     * Takes an {@link ThesaurusConcept} and adds it to index
     *
     * @param thesaurusConcept Updated/created {@link ThesaurusConcept} to save to index
     */
	void addConcept(ThesaurusConcept thesaurusConcept);

	/**
     * Takes a list of {@link ThesaurusConcept} and adds it to index
     *
     * @param thesaurusConcepts List of updated/created (@link Note) to save to index
     */
	void addConcepts(List<ThesaurusConcept> thesaurusConcepts);

	/**
     * Remove {@link ThesaurusConcept} from search index
     * @param {@link ThesaurusConcept}
     */
	void removeConcept(ThesaurusConcept thesaurusConcept);
}
