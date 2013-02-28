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
package fr.mcc.ginco.services;

import fr.mcc.ginco.beans.ThesaurusConcept;
import fr.mcc.ginco.beans.ThesaurusTerm;
import fr.mcc.ginco.beans.users.IUser;
import fr.mcc.ginco.exceptions.BusinessException;

import java.util.List;

public interface IThesaurusConceptService {
	/**
	 * Get list of all ThesaurusFormat.
	 * 
	 * @return
	 */
	List<ThesaurusConcept> getThesaurusConceptList();

	/**
	 * Get single ThesaurusFormat by its id.
	 * 
	 * @param id
	 *            of object
	 * @return {@code null} if not found; object otherwise.
	 */
	ThesaurusConcept getThesaurusConceptById(String id);

	/**
	 * Get the ThesaurusConcepts which are not top term in a given thesaurus
	 * 
	 * @param thesaurusId
	 * @return
	 */
	List<ThesaurusConcept> getOrphanThesaurusConcepts(String thesaurusId)
			throws BusinessException;

	/**
	 * Gets the preferred term of a concept
	 * 
	 * @param conceptId
	 * @return
	 * @throws BusinessException
	 */
	ThesaurusTerm getConceptPreferredTerm(String conceptId)
			throws BusinessException;

	/**
	 * Gets the label of a concept
	 * 
	 * @param conceptId
	 * @return
	 * @throws BusinessException
	 */
	String getConceptLabel(String conceptId) throws BusinessException;

	/**
	 * Create a single Thesaurus Concept Object
	 */
	ThesaurusConcept createThesaurusConcept(ThesaurusConcept object,
			List<ThesaurusTerm> terms, IUser user);

	/**
	 * Update a single Thesaurus Concept Object
	 */
	ThesaurusConcept updateThesaurusConcept(ThesaurusConcept object,
			List<ThesaurusTerm> terms, IUser user);

	/**
	 * Get the ThesaurusConcepts which are top term in a given thesaurus
	 * 
	 * @param thesaurusId
	 * @return
	 */
	List<ThesaurusConcept> getTopTermThesaurusConcepts(String thesaurusId)
			throws BusinessException;

	/**
	 * Get the number of orphan thesaurus concept for a given thesaurusId
	 * 
	 * @param thesaurusId
	 * @return
	 * @throws BusinessException
	 */
	long getOrphanThesaurusConceptsCount(String thesaurusId)
			throws BusinessException;

	/**
	 * Get the number of top concept for a given thesaurus
	 * 
	 * @param thesaurusId
	 * @return
	 * @throws BusinessException
	 */
	long getTopTermThesaurusConceptsCount(String thesaurusId)
			throws BusinessException;

    /**
     *
     * @param conceptId
     * @param thesaurusId
     * @return
     */
    List<ThesaurusConcept> getChildrenByConceptId(String conceptId, String thesaurusId);
}
