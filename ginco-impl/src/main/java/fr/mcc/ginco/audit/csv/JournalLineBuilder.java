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
package fr.mcc.ginco.audit.csv;

import fr.mcc.ginco.beans.GincoRevEntity;
import fr.mcc.ginco.beans.ThesaurusConcept;
import fr.mcc.ginco.beans.ThesaurusTerm;
import fr.mcc.ginco.exceptions.BusinessException;
import fr.mcc.ginco.services.IThesaurusConceptService;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.HashSet;
import java.util.Set;

/**
 * CSV export
 */
@Service("journalLineBuilder")
public class JournalLineBuilder {

	@Inject
	@Named("thesaurusConceptService")
	private IThesaurusConceptService thesaurusConceptService;

	/**
	 * Builds the basic common informations of a revision line
	 *
	 * @param event
	 * @param gincoRevEntity
	 * @return
	 */
	public JournalLine buildLineBase(JournalEventsEnum event,
	                                 GincoRevEntity gincoRevEntity) {
		JournalLine journal = new JournalLine();
		journal.setEventType(event);
		journal.setEventDate(gincoRevEntity.getRevisionDate());
		journal.setRevisionNumber(gincoRevEntity.getId());
		journal.setAuthorId(gincoRevEntity.getUsername());
		return journal;
	}


	/**
	 * Builds the revision line for the event of term creation
	 *
	 * @param term
	 * @param revision
	 * @return
	 */
	public JournalLine buildTermAddedLine(ThesaurusTerm term, GincoRevEntity revision) {

		JournalLine journalLine = buildLineBase(
				JournalEventsEnum.THESAURUSTERM_CREATED,
				revision);
		journalLine.setTermId(term.getIdentifier());
		journalLine.setNewLexicalValue(StringEscapeUtils.unescapeXml(term.getLexicalValue()));
		if (term.getConcept() != null) {
			journalLine.setConceptId(term.getConcept().getIdentifier());
		}
		return journalLine;
	}

	/**
	 * Builds the revision line for the event of term role change (preferred/non
	 * preferred)
	 *
	 * @param term
	 * @param revision
	 * @return
	 */
	public JournalLine buildTermRoleChangedLine(ThesaurusTerm term, GincoRevEntity revision) {
		JournalLine journal = buildLineBase(
				JournalEventsEnum.THESAURUSTERM_ROLE_UPDATE,
				revision);
		journal.setTermId(term.getIdentifier());
		if (term.getPrefered()) {
			journal.setTermRole("TP");
		} else {
			journal.setTermRole("TNP");
		}
		if (term.getConcept()!=null)
			journal.setConceptId(term.getConcept().getIdentifier());
		return journal;
	}

	/**
	 * Builds the list of revision lines for the event of term lexical value
	 * change
	 *
	 * @param term
	 * @param revision
	 * @param oldLexicalValue
	 * @return
	 */
	public JournalLine buildTermLexicalValueChangedLine(
			ThesaurusTerm term, GincoRevEntity revision, String oldLexicalValue) {
		JournalLine journal = buildLineBase(
				JournalEventsEnum.THESAURUSTERM_LEXICAL_VALUE_UPDATE,
				revision);
		journal.setTermId(term.getIdentifier());
		if (term.getConcept() != null) {
			journal.setConceptId(term.getConcept().getIdentifier());
		}
		journal.setNewLexicalValue(StringEscapeUtils.unescapeXml(term.getLexicalValue()));
		journal.setOldLexicalValue(StringEscapeUtils.unescapeXml(oldLexicalValue));

		return journal;
	}

	/**
	 * Builds the list of revision lines for the event of term attachment to a
	 * concept change
	 *
	 * @param term
	 * @param revision
	 * @param preferredTerm preferred term of the term concept in the previous version
	 * @return
	 */
	public JournalLine buildTermAttachmentChangedLine(
			ThesaurusTerm term, GincoRevEntity revision, ThesaurusTerm preferredTerm) {
		JournalLine journal = buildLineBase(
				JournalEventsEnum.THESAURUSTERM_LINKED_TO_CONCEPT,
				revision);
		journal.setTermId(term.getIdentifier());
		journal.setConceptId(term.getConcept().getIdentifier());
		journal.setNewLexicalValue(StringEscapeUtils.unescapeXml(term.getLexicalValue()));
		journal.setOldLexicalValue(StringEscapeUtils.unescapeXml(term.getLexicalValue()));
		return journal;
	}

	/**
	 * Builds the list of revision lines for the event of a concept hierarchy
	 * change
	 *
	 * @param conceptAtRevision
	 * @param revision
	 * @param oldGenericConcept
	 * @param currentGenericConcept
	 * @return
	 */
	public JournalLine buildConceptHierarchyChanged(
			ThesaurusConcept conceptAtRevision, GincoRevEntity revision,
			Set<ThesaurusConcept> oldGenericConcept, Set<ThesaurusConcept> currentGenericConcept) {
		JournalLine journal = buildLineBase(
				JournalEventsEnum.THESAURUSCONCEPT_HIERARCHY_UPDATE,
				revision);
		journal.setConceptId(conceptAtRevision.getIdentifier());
		journal.setNewGenericTerm(getConceptLabel(currentGenericConcept));

		journal.setOldGenericTerm(getConceptLabel(oldGenericConcept));

		return journal;
	}

	private Set<String> getConceptLabel(Set<ThesaurusConcept> concepts) {
		Set<String> conceptLabels = new HashSet<String>();
		for (ThesaurusConcept concept : concepts) {
			String conceptId = concept.getIdentifier();
			String conceptLexicalValue = "";
			try {
			   conceptLexicalValue = StringEscapeUtils.unescapeXml(thesaurusConceptService.getConceptLabel(concept.getIdentifier()));
			} catch (BusinessException bex)
			{
				
			}
			conceptLabels.add(conceptLexicalValue + " (" + conceptId + ")");
		}
		return conceptLabels;
	}
}
