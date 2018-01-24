/*
jboard is a java bulletin board.
version $Name:  $
http://sourceforge.net/projects/jboard/
Copyright (C) 2003 Charles GAY
This file is part of jboard.
jboard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

jboard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with jboard; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package net.sf.jboard.model.common;

import java.io.Serializable;


/**
 * <p>
 * Manifest constants for the whole application.
 * </p>
 *
 * @author <a href="mailto:diabolo512@users.sourceforge.net ">Charles Gay</a>
 * @version $Revision: 1.4 $
 */
public final class Constants implements Serializable {

    /**
     * Session scope attribute key under which the previous servlet path is
     * kept.
     */
    public static final String PREVIOUS_URL_KEY = "callerURI";

    /**
     * Application scope attribute key under which the {@link ForumDAOInterface}
     * implementation is kept.
     */
    public static final String DAO_KEY = "DAOImplementation";

    /**
     * Application scope attribute key under which the {@link Config} object is
     * kept.
     */
    public static final String CONFIG_KEY = "configuration";

    /** Attribute key under which the {@link PollForm} form bean is kept. */
    public static final String FORM_KEY = "pollForm";

    /**
     * Request scope attribute key under which the {@link VoteForm} form bean
     * is kept.
     */
    public static final String VOTE_FORM = "voteForm";

    /** Attribute key under which the active {@link Poll} bean is kept. */
    public static final String ACTIVE_POLL_KEY = "activePoll";

    /**
     * Attribute key under which the id for the active {@link Poll} bean is
     * kept.
     */
    public static final String ACTIVE_POLL_ID_KEY = "activePollId";

    /**
     * Attribute key under which the {@link Results} for the active poll is
     * kept.
     */
    public static final String RESULTS_KEY = "pollResults";

    /**
     * Request scope attribute key under which the list of {@link Poll}s is
     * kept.
     */
    public static final String POLLS_COLLECTION = "pollsList";

    /**
     * Request scope attribute key under which the list of {@link Results}s is
     * kept.
     */
    public static final String RESULTS_COLLECTION = "resultsList";

    /** The resource key identifier for the failure URI. */
    public static final String FAILURE_KEY = "failure";

    /** The resource key identifier for the success URI. */
    public static final String SUCCESS_KEY = "success";

    /** The resource key identifier for the cancel URI. */
    public static final String CANCEL_KEY = "cancel";

    /** The resource key identifier for the submit request parameter. */
    public static final String SUBMIT_KEY = "submit";

    /** The resource key identifier for the survey option 1. */
    public static final String QUESTION_1 = "q1";

    /** The resource key identifier for the survey option 2. */
    public static final String QUESTION_2 = "q2";

    /** The resource key identifier for the survey option 3. */
    public static final String QUESTION_3 = "q3";

    /** The resource key identifier for the survey option 4. */
    public static final String QUESTION_4 = "q4";

    /** The resource key identifier for the survey option 5. */
    public static final String QUESTION_5 = "q5";

    /** The resource key identifier for the survey option 6. */
    public static final String QUESTION_6 = "q6";

    /** The resource key identifier for the survey option 7. */
    public static final String QUESTION_7 = "q7";

    /** The resource key identifier for the survey option 8. */
    public static final String QUESTION_8 = "q8";

    /** The resource key identifier for the survey option 9. */
    public static final String QUESTION_9 = "q9";

    /** The resource key identifier for the survey option 10. */
    public static final String QUESTION_10 = "q10";

    /** The resource key identifier for the survey answer 1. */
    public static final String ANSWER_1 = "a1";

    /** The resource key identifier for the survey answer 2. */
    public static final String ANSWER_2 = "a2";

    /** The resource key identifier for the survey answer 3. */
    public static final String ANSWER_3 = "a3";

    /** The resource key identifier for the survey answer 4. */
    public static final String ANSWER_4 = "a4";

    /** The resource key identifier for the survey answer 5. */
    public static final String ANSWER_5 = "a5";

    /** The resource key identifier for the survey answer 6. */
    public static final String ANSWER_6 = "a6";

    /** The resource key identifier for the survey answer 7. */
    public static final String ANSWER_7 = "a7";

    /** The resource key identifier for the survey answer 8. */
    public static final String ANSWER_8 = "a8";

    /** The resource key identifier for the survey answer 9. */
    public static final String ANSWER_9 = "a9";

    /** The resource key identifier for the survey answer 10. */
    public static final String ANSWER_10 = "a10";

    /** The resource key identifier for the id request parameter. */
    public static final String ID_KEY = "id";

    /** The resource key identifier for the name request parameter. */
    public static final String NAME_KEY = "name";

    /** The resource key identifier for the type request parameter. */
    public static final String TYPE_KEY = "type";

    /** The resource key identifier for the expiry parameter. */
    public static final String EXPIRY_KEY = "expiry";

    /** The resource key identifier for the vote parameter. */
    public static final String VOTE_KEY = "vote";

    /**
     * The resource key identifier for the attribute under which the error
     * messages are kept.
     */
    public static final String ERRORS_KEY = "polls.errors";

    /**
     * The resource key identifier for the attribute under which the
     * transaction control token messages are kept.
     */
    public static final String TOKEN_KEY = "token";

    /**
     * The resource key identifier for the attribute under which the locale id
     * is kept.
     */
    public static final String LOCALE_KEY = "locale";

    /** The resource key identifier for a bar chart. */
    public static final String BAR_CHART_KEY = "bar";

    /** The resource key identifier for a pie chart. */
    public static final String PIE_CHART_KEY = "pie";

}
