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
package fr.mcc.ginco.exports;

import fr.mcc.ginco.beans.*;
import fr.mcc.ginco.exceptions.BusinessException;
import fr.mcc.ginco.exports.result.bean.FormattedLine;
import fr.mcc.ginco.services.*;
import fr.mcc.ginco.utils.LabelUtil;
import fr.mcc.ginco.utils.ThesaurusTermUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Service("exportService")
public class ExportServiceImpl implements IExportService {
	@Inject
	@Named("thesaurusArrayService")
	private IThesaurusArrayService thesaurusArrayService;

	@Inject
	@Named("thesaurusTermService")
	private IThesaurusTermService thesaurusTermService;

	@Inject
	@Named("nodeLabelService")
	private INodeLabelService nodeLabelService;

	@Inject
	@Named("thesaurusConceptService")
	private IThesaurusConceptService thesaurusConceptService;

	@Inject
	@Named("noteService")
	private INoteService noteService;

	@Inject
	@Named("associativeRelationshipService")
	private IAssociativeRelationshipService associativeRelationshipService;

	@Value("${ginco.default.language}")
	private String defaultLang;

	@Inject
	@Named("thesaurusTermRoleService")
	private IThesaurusTermRoleService thesaurusTermRoleService;

	@Override
	public List<FormattedLine> getHierarchicalText(Thesaurus thesaurus)
			throws BusinessException {
		List<ThesaurusConcept> listTT = thesaurusConceptService
				.getTopTermThesaurusConcepts(thesaurus.getIdentifier());
		Collections.sort(listTT, new ThesaurusConceptComparator());
		List<ThesaurusArray> orphanArrays = thesaurusArrayService
				.getArraysWithoutParentConcept(thesaurus.getIdentifier());

		Set<ThesaurusConcept> exclude = new HashSet<ThesaurusConcept>();

		for (ThesaurusArray array : orphanArrays) {
			exclude.addAll(array.getConcepts());
		}

		List<FormattedLine> result = new ArrayList<FormattedLine>();

		for (ThesaurusConcept conceptTT : listTT) {
			if (!exclude.contains(conceptTT)) {
				result.addAll(getHierarchicalText(0, conceptTT));
			}
		}

		for (ThesaurusArray array : orphanArrays) {
			addThesaurusArray(result, array, -1);
		}

		return result;
	}

	@Override
	public List<FormattedLine> getAlphabeticalText(Thesaurus thesaurus)
			throws BusinessException {
		List<FormattedLine> result = new ArrayList<FormattedLine>();

		List<ThesaurusConcept> concepts = thesaurusConceptService
				.getConceptsByThesaurusId(null, thesaurus.getThesaurusId(),
						null, Boolean.TRUE);

		Collections.sort(concepts, new ThesaurusConceptComparator());

		for (ThesaurusConcept concept : concepts) {
			addConceptTitle(0, result, concept);
			addConceptInfo(1, result, concept);
		}

		return result;
	}

	private void addConceptTitle(Integer base, List<FormattedLine> result,
			ThesaurusConcept concept) {
		result.add(new FormattedLine(base, thesaurusConceptService
				.getConceptTitle(concept)));
	}

	/**
	 * For alphabetic export.
	 * 
	 * banquette NA: Siège à plusieurs places, peu profond, garni, comportant
	 * éventuellement soit un dossier, soit des accotoirs, soit les deux. EP:
	 * banquette à accotoirs banquette à dossier TG: siège banquette à accotoirs
	 * EM: banquette banquette à dossier EM: banquette
	 */
	private void addConceptInfo(Integer base, List<FormattedLine> result,
			ThesaurusConcept concept) {
		List<Note> notes = noteService.getConceptNotePaginatedList(
				concept.getIdentifier(), 0, 0);

        List<ThesaurusTerm> prefTerms = thesaurusConceptService.getConceptPreferredTerms(concept.getIdentifier());

        if(concept.getNotation() != null && !concept.getNotation().isEmpty()) {
            result.add(new FormattedLine(base, LabelUtil.getResourceLabel("CC")
                    + ": "
                    + concept.getNotation()));
        }

		for (Note note : notes) {
			if ("scopeNote".equals(note.getNoteType().getCode())) {
				result.add(new FormattedLine(base, LabelUtil.getResourceLabel("NA")
                        + ": "
						+ note.getLexicalValue()));
			}
		}

		for (ThesaurusConcept parent : concept.getParentConcepts()) {

            List<ThesaurusTerm> parentPrefs =
                    thesaurusConceptService.getConceptPreferredTerms(parent.getIdentifier());

            result.add(new FormattedLine(base, LabelUtil.getResourceLabel("TG")
                    + ": "
					+ generatePrefTermsText(parentPrefs)));
		}

		for (ThesaurusConcept ta : thesaurusConceptService
				.getThesaurusConceptList(associativeRelationshipService
						.getAssociatedConceptsId(concept))) {

            List<ThesaurusTerm> taPrefs =
                    thesaurusConceptService.getConceptPreferredTerms(ta.getIdentifier());

            result.add(new FormattedLine(base, LabelUtil.getResourceLabel("TA")
                    + ": "
					+ generatePrefTermsText(taPrefs)));
		}

		for (ThesaurusTerm term : thesaurusTermService
				.getTermsByConceptId(concept.getIdentifier())) {
			if (!term.getPrefered()) {
				result.add(new FormattedLine(base, LabelUtil.getResourceLabel("EP")
                        + ": "
						+ LabelUtil.getLocalizedLabel(term.getLexicalValue(),
								term.getLanguage(), defaultLang)));
			}
		}

		for (ThesaurusConcept child : thesaurusConceptService
				.getChildrenByConceptId(concept.getIdentifier())) {

            ThesaurusTerm term = thesaurusConceptService.getConceptPreferredTerm(child.getIdentifier());

			result.add(new FormattedLine(base, LabelUtil.getResourceLabel("TS")
                    + ": "
					+ LabelUtil.getLocalizedLabel(
                    term.getLexicalValue(), term.getLanguage(),
                    defaultLang)));
		}

		for (ThesaurusTerm term :
                thesaurusConceptService.getConceptNotPreferredTerms(concept.getIdentifier())) {
			    result.add(new FormattedLine(base - 1, LabelUtil
						.getLocalizedLabel(term.getLexicalValue(),
								term.getLanguage(), defaultLang)));
				if (term.getRole() == null) {
					result.add(new FormattedLine(base, thesaurusTermRoleService
							.getDefaultThesaurusTermRole().getCode()
							+ ": "
							+ generatePrefTermsText(prefTerms)));
				} else {
					result.add(new FormattedLine(base, term.getRole().getCode()
							+ ": "
							+ generatePrefTermsText(prefTerms)));
				}
		}
	}

	private List<FormattedLine> getHierarchicalText(Integer base,
			ThesaurusConcept concept) throws BusinessException {
		List<FormattedLine> result = new ArrayList<FormattedLine>();

		Set<String> thesaurusArrayConcepts = new HashSet<String>();

		addConceptTitle(base, result, concept);

		List<ThesaurusArray> subOrdArrays = thesaurusArrayService
				.getSubOrdinatedArrays(concept.getIdentifier());

		for (ThesaurusArray subOrdArray : subOrdArrays) {
			for (ThesaurusConcept conceptInArray : subOrdArray.getConcepts()) {
				thesaurusArrayConcepts.add(conceptInArray.getIdentifier());
			}
		}

		List<ThesaurusConcept> children = new ArrayList<ThesaurusConcept>(
				thesaurusConceptService.getChildrenByConceptId(concept
						.getIdentifier()));
		Collections.sort(children, new ThesaurusConceptComparator());

		for (ThesaurusConcept child : children) {
			if (!thesaurusArrayConcepts.contains(child.getIdentifier())) {
				result.addAll(getHierarchicalText(base + 1, child));
			}
		}

		for (ThesaurusArray subOrdArray : subOrdArrays) {
			addThesaurusArray(result, subOrdArray, base);
		}

		return result;
	}

    private String generatePrefTermsText(List<ThesaurusTerm> terms) {

        String result = "";

        if(terms.size() == 1) {
            result = LabelUtil.getLocalizedLabel(
                    terms.get(0).getLexicalValue(), terms.get(0).getLanguage(),
                    defaultLang);
        } else {
            for(ThesaurusTerm term : terms) {
                result += term.getLexicalValue() + " (" + term.getLanguage().getId() + "), ";
            }
            result = result.substring(0, result.length()-2);
        }

        return result;
    }

	private void addThesaurusArray(List<FormattedLine> result,
			ThesaurusArray subOrdArray, Integer base) throws BusinessException {
		result.add(new FormattedLine(base + 1, "<"
				+ nodeLabelService.getByThesaurusArray(
						subOrdArray.getIdentifier()).getLexicalValue() + ">"));
		List<ThesaurusConcept> conceptsInArray = new ArrayList<ThesaurusConcept>(
				subOrdArray.getConcepts());
		Collections.sort(conceptsInArray, new ThesaurusConceptComparator());

		for (ThesaurusConcept conceptInArray : conceptsInArray) {
			result.addAll(getHierarchicalText(base + 1, conceptInArray));
		}
	}

	/**
	 * Comparator to use with two concepts - compares based on its lexicalValue.
	 */
	class ThesaurusConceptComparator implements Comparator<ThesaurusConcept> {
		@Override
		public int compare(ThesaurusConcept o1, ThesaurusConcept o2) {
			try {
				String l1 = ThesaurusTermUtils
						.getPreferedTerms(
								thesaurusTermService.getTermsByConceptId(o1
										.getIdentifier())).get(0)
						.getLexicalValue();
				String l2 =ThesaurusTermUtils
						.getPreferedTerms(
								thesaurusTermService.getTermsByConceptId(o2
										.getIdentifier())).get(0)
						.getLexicalValue();
				return l1.compareToIgnoreCase(l2);
			} catch (BusinessException e) {
				return 0;
			}
		}
	}
}
