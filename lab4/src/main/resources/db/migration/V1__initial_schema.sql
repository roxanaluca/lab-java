--
-- PostgreSQL database dump
--

-- Dumped from database version 16.10 (Debian 16.10-1.pgdg13+1)

SET default_tablespace = '';



--
-- Name: courses; Type: TABLE; Schema: public; Owner: appuser
--

CREATE TABLE public.courses (
                                id bigint NOT NULL,
                                type character varying(255) NOT NULL,
                                code character varying(255) NOT NULL,
                                abbr character varying(255),
                                name character varying(255) NOT NULL,
                                instructor_id bigint,
                                pack_id bigint,
                                group_count integer DEFAULT 1,
                                description character varying(255),
                                CONSTRAINT courses_group_count_check CHECK ((group_count >= 0))
);


ALTER TABLE public.courses OWNER TO appuser;

--
-- Name: courses_id_seq; Type: SEQUENCE; Schema: public; Owner: appuser
--

CREATE SEQUENCE public.courses_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.courses_id_seq OWNER TO appuser;

--
-- Name: courses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: appuser
--

ALTER SEQUENCE public.courses_id_seq OWNED BY public.courses.id;


--
-- Name: customers; Type: TABLE; Schema: public; Owner: appuser
--

CREATE TABLE public.customers (
                                  id bigint NOT NULL,
                                  email character varying(180) NOT NULL,
                                  full_name character varying(120) NOT NULL,
                                  signup_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                  is_active boolean DEFAULT true NOT NULL
);


ALTER TABLE public.customers OWNER TO appuser;

--
-- Name: customers_id_seq; Type: SEQUENCE; Schema: public; Owner: appuser
--

ALTER TABLE public.customers ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.customers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
    );


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: appuser
--

--
-- Name: instructors; Type: TABLE; Schema: public; Owner: appuser
--

CREATE TABLE public.instructors (
                                    id bigint NOT NULL,
                                    name character varying(255) NOT NULL,
                                    email character varying(255),
                                    password character varying(255) DEFAULT '$2a$10$Q9pPbaEiVYtklrRkA3kU6e1pBQrge0iEUeR1sOJXs6wrFlo6TbScG'::character varying NOT NULL
);


ALTER TABLE public.instructors OWNER TO appuser;

--
-- Name: instructors_id_seq; Type: SEQUENCE; Schema: public; Owner: appuser
--

CREATE SEQUENCE public.instructors_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.instructors_id_seq OWNER TO appuser;

--
-- Name: instructors_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: appuser
--

ALTER SEQUENCE public.instructors_id_seq OWNED BY public.instructors.id;


--
-- Name: packs; Type: TABLE; Schema: public; Owner: appuser
--

CREATE TABLE public.packs (
                              id bigint NOT NULL,
                              year integer NOT NULL,
                              semester integer NOT NULL,
                              name character varying(255) NOT NULL,
                              last_updated timestamp(6) with time zone,
                              CONSTRAINT packs_semester_check CHECK ((semester = ANY (ARRAY[1, 2]))),
                              CONSTRAINT packs_year_check CHECK ((year >= 2000))
);


ALTER TABLE public.packs OWNER TO appuser;

--
-- Name: packs_id_seq; Type: SEQUENCE; Schema: public; Owner: appuser
--

CREATE SEQUENCE public.packs_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.packs_id_seq OWNER TO appuser;

--
-- Name: packs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: appuser
--

ALTER SEQUENCE public.packs_id_seq OWNED BY public.packs.id;


--
-- Name: persons_id_seq; Type: SEQUENCE; Schema: public; Owner: appuser
--

CREATE SEQUENCE public.persons_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.persons_id_seq OWNER TO appuser;

--
-- Name: preference_items; Type: TABLE; Schema: public; Owner: appuser
--

CREATE TABLE public.preference_items (
                                         preference_id bigint NOT NULL,
                                         course_id bigint NOT NULL,
                                         pack_id bigint,
                                         rank double precision NOT NULL
);


ALTER TABLE public.preference_items OWNER TO appuser;

--
-- Name: preferences; Type: TABLE; Schema: public; Owner: appuser
--

CREATE TABLE public.preferences (
                                    id bigint NOT NULL,
                                    student_id bigint NOT NULL,
                                    last_updated timestamp(6) with time zone
);


ALTER TABLE public.preferences OWNER TO appuser;

--
-- Name: preferences_id_seq; Type: SEQUENCE; Schema: public; Owner: appuser
--

CREATE SEQUENCE public.preferences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.preferences_id_seq OWNER TO appuser;

--
-- Name: preferences_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: appuser
--

ALTER SEQUENCE public.preferences_id_seq OWNED BY public.preferences.id;


--
-- Name: student_course; Type: TABLE; Schema: public; Owner: appuser
--

CREATE TABLE public.student_course (
                                       course_id bigint NOT NULL,
                                       student_id bigint NOT NULL
);


ALTER TABLE public.student_course OWNER TO appuser;

--
-- Name: student_grade_id_seq; Type: SEQUENCE; Schema: public; Owner: appuser
--

CREATE SEQUENCE public.student_grade_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.student_grade_id_seq OWNER TO appuser;

--
-- Name: student_grades; Type: TABLE; Schema: public; Owner: appuser
--

CREATE TABLE public.student_grades (
                                       id bigint NOT NULL,
                                       student_id bigint NOT NULL,
                                       course_id bigint NOT NULL,
                                       grade numeric(38,2) NOT NULL,
                                       created_at timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.student_grades OWNER TO appuser;

--
-- Name: student_grades_id_seq; Type: SEQUENCE; Schema: public; Owner: appuser
--

CREATE SEQUENCE public.student_grades_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.student_grades_id_seq OWNER TO appuser;

--
-- Name: student_grades_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: appuser
--

ALTER SEQUENCE public.student_grades_id_seq OWNED BY public.student_grades.id;


--
-- Name: students; Type: TABLE; Schema: public; Owner: appuser
--

CREATE TABLE public.students (
                                 id bigint NOT NULL,
                                 code character varying(255) NOT NULL,
                                 name character varying(255) NOT NULL,
                                 email character varying(255),
                                 year integer,
                                 password character varying(255) DEFAULT '$2a$10$Q9pPbaEiVYtklrRkA3kU6e1pBQrge0iEUeR1sOJXs6wrFlo6TbScG'::character varying NOT NULL,
                                 CONSTRAINT students_year_check CHECK (((year >= 2000) AND (year <= 2025)))
);


ALTER TABLE public.students OWNER TO appuser;

--
-- Name: students_id_seq; Type: SEQUENCE; Schema: public; Owner: appuser
--

CREATE SEQUENCE public.students_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.students_id_seq OWNER TO appuser;

--
-- Name: students_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: appuser
--

ALTER SEQUENCE public.students_id_seq OWNED BY public.students.id;


--
-- Name: courses id; Type: DEFAULT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.courses ALTER COLUMN id SET DEFAULT nextval('public.courses_id_seq'::regclass);


--
-- Name: instructors id; Type: DEFAULT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.instructors ALTER COLUMN id SET DEFAULT nextval('public.instructors_id_seq'::regclass);


--
-- Name: packs id; Type: DEFAULT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.packs ALTER COLUMN id SET DEFAULT nextval('public.packs_id_seq'::regclass);


--
-- Name: preferences id; Type: DEFAULT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.preferences ALTER COLUMN id SET DEFAULT nextval('public.preferences_id_seq'::regclass);


--
-- Name: student_grades id; Type: DEFAULT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.student_grades ALTER COLUMN id SET DEFAULT nextval('public.student_grades_id_seq'::regclass);


--
-- Name: students id; Type: DEFAULT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.students ALTER COLUMN id SET DEFAULT nextval('public.students_id_seq'::regclass);


--
-- Data for Name: courses; Type: TABLE DATA; Schema: public; Owner: appuser
--

INSERT INTO public.courses VALUES
                               (3303, 'Lecture', 'CS153', 'CS', 'The Mirror Crack''d from Side to Side', 96, 3253, 8, 'Enim excepturi voluptatem nulla necessitatibus.'),
                               (3304, 'Lecture', 'CS146', 'CS', 'Lilies of the Field', 96, 3254, 3, 'Nihil commodi et omnis voluptas minima et sit.'),
                               (3305, 'Lab', 'CS112', 'CS', 'The Proper Study', 96, 3254, 9, 'Ab modi atque laudantium laborum qui eaque cumque.'),
                               (3306, 'Lecture', 'CS164', 'CS', 'The Mirror Crack''d from Side to Side', 96, 3253, 3, 'Harum quis consectetur temporibus consequatur quas.'),
                               (3307, 'Lecture', 'CS127', 'CS', 'The Golden Apples of the Sun', 96, 3253, 8, 'Natus soluta sit.'),
                               (3403, 'Lab', 'CS187', 'CS', 'From Here to Eternity', 99, 3353, 3, 'Officia adipisci blanditiis accusantium nihil cumque beatae ea.'),
                               (3004, 'Lecture', 'CS101', 'CS', 'Things Fall Apart', 87, 2953, 2, 'Mollitia quo nesciunt quam quasi.'),
                               (3005, 'Lecture', 'CS142', 'CS', 'Eyeless in Gaza', 86, 2954, 1, 'Quae quis praesentium quisquam perspiciatis ut aut ducimus.'),
                               (3006, 'Lecture', 'CS199', 'CS', 'For a Breath I Tarry', 86, 2953, 5, 'Aliquid ab voluptatem eos magni ullam nulla et.'),
                               (3007, 'Lecture', 'CS118', 'CS', 'Paths of Glory', 87, 2953, 2, 'Sapiente quasi quod ullam voluptas.'),
                               (3405, 'Lab', 'CS161', 'CS', 'The Soldier''s Art', 99, 3353, 6, 'Esse in adipisci.'),
                               (3103, 'Lab', 'CS179', 'CS', 'No Longer at Ease', 90, 3053, 7, 'Inventore ut enim suscipit sed non.'),
                               (3104, 'Lab', 'CS128', 'CS', 'Antic Hay', 90, 3053, 3, 'Ab est quo.'),
                               (3105, 'Lab', 'CS144', 'CS', 'The Waste Land', 90, 3054, 3, 'Nemo eius praesentium.'),
                               (3107, 'Lecture', 'CS177', 'CS', 'Eyeless in Gaza', 89, 3054, 8, 'Laboriosam minima nihil cumque similique aperiam.'),
                               (3203, 'Lecture', 'CS107', 'CS', 'The Curious Incident of the Dog in the Night-Time', 93, 3154, 9, 'Voluptatem sit voluptas in.'),
                               (3204, 'Lab', 'CS188', 'CS', 'A Time of Gifts', 93, 3153, 2, 'Quos natus quod omnis magni nobis assumenda dolor.'),
                               (3205, 'Lecture', 'CS168', 'CS', 'That Hideous Strength', 93, 3154, 7, 'Eum molestiae expedita aut doloribus et.'),
                               (3206, 'Lecture', 'CS170', 'CS', 'Endless Night', 93, 3154, 5, 'Voluptatibus error quidem est dignissimos voluptatem eligendi.'),
                               (3407, 'Lecture', 'CS198', 'CS', 'Look Homeward, Angel', 99, 3354, 10, 'Iure enim qui similique tempore repudiandae doloribus quam.'),
                               (3408, 'Lecture', 'CS102', 'CS', 'Look Homeward, Angel', 99, NULL, 10, 'Iure enim qui similique tempore repudiandae doloribus quam.');


--
-- Data for Name: customers; Type: TABLE DATA; Schema: public; Owner: appuser
--



--
-- Data for Name: instructors; Type: TABLE DATA; Schema: public; Owner: appuser
--

INSERT INTO public.instructors VALUES
                                   (86, 'Anneliese Koepp', 'santo.gaylord@hotmail.com', '$2a$10$Q9pPbaEiVYtklrRkA3kU6e1pBQrge0iEUeR1sOJXs6wrFlo6TbScG'),
                                   (87, 'Mr. Edda Bergnaum', 'irvin.stehr@gmail.com', '$2a$10$Q9pPbaEiVYtklrRkA3kU6e1pBQrge0iEUeR1sOJXs6wrFlo6TbScG'),
                                   (89, 'Arthur Stehr', 'kelley.smitham@hotmail.com', '$2a$10$Q9pPbaEiVYtklrRkA3kU6e1pBQrge0iEUeR1sOJXs6wrFlo6TbScG'),
                                   (90, 'Sherwood Smith', 'katlyn.leannon@yahoo.com', '$2a$10$Q9pPbaEiVYtklrRkA3kU6e1pBQrge0iEUeR1sOJXs6wrFlo6TbScG'),
                                   (92, 'Eliseo Langworth PhD', 'lucia.beatty@hotmail.com', '$2a$10$Q9pPbaEiVYtklrRkA3kU6e1pBQrge0iEUeR1sOJXs6wrFlo6TbScG'),
                                   (93, 'Williams Hackett V', 'tessa.turner@gmail.com', '$2a$10$Q9pPbaEiVYtklrRkA3kU6e1pBQrge0iEUeR1sOJXs6wrFlo6TbScG'),
                                   (95, 'Iona Bernhard', 'josh.reinger@hotmail.com', '$2a$10$Q9pPbaEiVYtklrRkA3kU6e1pBQrge0iEUeR1sOJXs6wrFlo6TbScG'),
                                   (96, 'Jasmine Greenfelder', 'fritz.legros@gmail.com', '$2a$10$Q9pPbaEiVYtklrRkA3kU6e1pBQrge0iEUeR1sOJXs6wrFlo6TbScG'),
                                   (98, 'Elsy Stokes I', 'maureen.baumbach@hotmail.com', '$2a$10$Q9pPbaEiVYtklrRkA3kU6e1pBQrge0iEUeR1sOJXs6wrFlo6TbScG'),
                                   (99, 'Tu Hand', 'lillia.hammes@gmail.com', '$2a$10$Q9pPbaEiVYtklrRkA3kU6e1pBQrge0iEUeR1sOJXs6wrFlo6TbScG'),
                                   (106, 'Alice Doe', 'alice1.doe@example.com', '$2a$10$Q9pPbaEiVYtklrRkA3kU6e1pBQrge0iEUeR1sOJXs6wrFlo6TbScG'),
                                   (108, 'Alice Doe', 'alice.doe@example.com', '$2a$10$Q9pPbaEiVYtklrRkA3kU6e1pBQrge0iEUeR1sOJXs6wrFlo6TbScG');


--
-- Data for Name: packs; Type: TABLE DATA; Schema: public; Owner: appuser
--

INSERT INTO public.packs VALUES
                             (153, 2025, 1, 'Spring 2025', NULL),
                             (154, 2025, 2, 'Autumn 2025', NULL),
                             (253, 2025, 1, 'Spring 2025', NULL),
                             (254, 2025, 2, 'Autumn 2025', NULL),
                             (353, 2025, 1, 'Spring 2025', NULL),
                             (354, 2025, 2, 'Autumn 2025', NULL),
                             (453, 2025, 1, 'Spring 2025', NULL),
                             (454, 2025, 2, 'Autumn 2025', NULL),
                             (553, 2025, 1, 'Spring 2025', NULL),
                             (554, 2025, 2, 'Autumn 2025', NULL),
                             (2953, 2025, 1, 'Spring 2025', NULL),
                             (653, 2025, 1, 'Spring 2025', NULL),
                             (654, 2025, 2, 'Autumn 2025', NULL),
                             (2954, 2025, 2, 'Autumn 2025', NULL),
                             (753, 2025, 1, 'Spring 2025', NULL),
                             (754, 2025, 2, 'Autumn 2025', NULL),
                             (853, 2025, 1, 'Spring 2025', NULL),
                             (854, 2025, 2, 'Autumn 2025', NULL),
                             (952, 2025, 2, 'Autumn 2025 Courses', NULL),
                             (3154, 2025, 2, 'Autumn 2025', NULL),
                             (3253, 2025, 1, 'Spring 2025', NULL),
                             (3254, 2025, 2, 'Autumn 2025', NULL),
                             (3452, 2025, 2, 'Autumn 2025 Courses', '2025-11-11 10:01:30.861557+00'),
                             (3552, 2025, 2, 'Autumn 2025 Courses', '2025-11-11 10:10:25.961501+00'),
                             (3652, 2025, 2, 'Autumn 2025 Courses', '2025-11-11 10:12:10.777922+00'),
                             (2552, 2025, 2, 'Autumn 2025 Courses', '2025-12-01 11:38:26.883213+00'),
                             (3054, 2025, 2, 'Autumn 2025', '2025-12-01 11:41:05.107718+00'),
                             (3153, 2025, 1, 'Spring 2025', '2025-12-01 11:44:21.255075+00'),
                             (3354, 2025, 2, 'Autumn 2025', '2025-12-01 11:46:33.708238+00'),
                             (3353, 2025, 1, 'Spring 2025', '2025-12-01 11:47:14.027155+00'),
                             (3053, 2025, 1, 'Spring 2025', '2025-12-01 11:48:08.07544+00'),
                             (2453, 2025, 1, 'Spring 2025', NULL),
                             (2454, 2025, 2, 'Autumn 2025', NULL);


--
-- Data for Name: preference_items; Type: TABLE DATA; Schema: public; Owner: appuser
--

INSERT INTO public.preference_items VALUES
                                        (4, 3407, 3354, 0),
                                        (4, 3403, 3353, 1),
                                        (4, 3103, 3053, 2);


--
-- Data for Name: preferences; Type: TABLE DATA; Schema: public; Owner: appuser
--

INSERT INTO public.preferences VALUES
    (4, 13, '2025-12-01 12:06:36.522428+00');


--
-- Data for Name: student_course; Type: TABLE DATA; Schema: public; Owner: appuser
--

INSERT INTO public.student_course VALUES
    (3408, 13);


--
-- Data for Name: student_grades; Type: TABLE DATA; Schema: public; Owner: appuser
--

INSERT INTO public.student_grades VALUES
                                      (1, 13, 3408, 9.50, '2025-12-09 05:47:57.488718+00'),
                                      (2, 13, 3408, 8.00, '2025-12-09 05:47:57.542551+00'),
                                      (52, 13, 3408, 10.00, '2025-12-09 09:33:48.185603+00'),
                                      (53, 13, 3408, 10.00, '2025-12-09 09:34:05.085576+00'),
                                      (54, 13, 3408, 7.00, '2025-12-09 09:34:06.540018+00'),
                                      (102, 13, 3004, 7.00, '2025-12-09 09:50:41.351381+00'),
                                      (103, 13, 3004, 10.00, '2025-12-09 09:50:43.614488+00');


--
-- Data for Name: students; Type: TABLE DATA; Schema: public; Owner: appuser
--

INSERT INTO public.students VALUES
                                (8, '1228839823', 'Popescu Adelina', 'blabla', 2025, '$2a$10$Q9pPbaEiVYtklrRkA3kU6e1pBQrge0iEUeR1sOJXs6wrFlo6TbScG'),
                                (12, '12288393838823', 'Popescu Adelina', 'blabla@gmail.com', 2025, '$2a$10$Q9pPbaEiVYtklrRkA3kU6e1pBQrge0iEUeR1sOJXs6wrFlo6TbScG'),
                                (13, 'MRL', 'MRL', 'roxanaluca10@gmail.com', 2025, '$2a$10$r3VuoqMklRTNVIVXKDqABOBDKnAtccRxzVPQ3z2aYRx4pjHFGRp1G');


--
-- Name: courses_id_seq; Type: SEQUENCE SET; Schema: public; Owner: appuser
--

SELECT pg_catalog.setval('public.courses_id_seq', 1, false);


--
-- Name: customers_id_seq; Type: SEQUENCE SET; Schema: public; Owner: appuser
--

SELECT pg_catalog.setval('public.customers_id_seq', 1, false);


--
-- Name: instructors_id_seq; Type: SEQUENCE SET; Schema: public; Owner: appuser
--

SELECT pg_catalog.setval('public.instructors_id_seq', 109, true);


--
-- Name: packs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: appuser
--

SELECT pg_catalog.setval('public.packs_id_seq', 1, false);


--
-- Name: persons_id_seq; Type: SEQUENCE SET; Schema: public; Owner: appuser
--

SELECT pg_catalog.setval('public.persons_id_seq', 3751, true);


--
-- Name: preferences_id_seq; Type: SEQUENCE SET; Schema: public; Owner: appuser
--

SELECT pg_catalog.setval('public.preferences_id_seq', 4, true);


--
-- Name: student_grade_id_seq; Type: SEQUENCE SET; Schema: public; Owner: appuser
--

SELECT pg_catalog.setval('public.student_grade_id_seq', 151, true);


--
-- Name: student_grades_id_seq; Type: SEQUENCE SET; Schema: public; Owner: appuser
--

SELECT pg_catalog.setval('public.student_grades_id_seq', 1, false);


--
-- Name: students_id_seq; Type: SEQUENCE SET; Schema: public; Owner: appuser
--

SELECT pg_catalog.setval('public.students_id_seq', 17, true);



--
-- Name: courses courses_code_key; Type: CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_code_key UNIQUE (code);


--
-- Name: courses courses_pkey; Type: CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_pkey PRIMARY KEY (id);


--
-- Name: customers customers_email_key; Type: CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT customers_email_key UNIQUE (email);


--
-- Name: customers customers_pkey; Type: CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.customers
    ADD CONSTRAINT customers_pkey PRIMARY KEY (id);


--
-- Name: instructors instructors_email_key; Type: CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.instructors
    ADD CONSTRAINT instructors_email_key UNIQUE (email);


--
-- Name: instructors instructors_pkey; Type: CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.instructors
    ADD CONSTRAINT instructors_pkey PRIMARY KEY (id);


--
-- Name: packs packs_pkey; Type: CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.packs
    ADD CONSTRAINT packs_pkey PRIMARY KEY (id);


--
-- Name: preference_items preference_items_pkey; Type: CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.preference_items
    ADD CONSTRAINT preference_items_pkey PRIMARY KEY (preference_id, course_id);


--
-- Name: preferences preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.preferences
    ADD CONSTRAINT preferences_pkey PRIMARY KEY (id);


--
-- Name: student_course student_course_pkey; Type: CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.student_course
    ADD CONSTRAINT student_course_pkey PRIMARY KEY (course_id, student_id);


--
-- Name: student_grades student_grades_pkey; Type: CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.student_grades
    ADD CONSTRAINT student_grades_pkey PRIMARY KEY (id);


--
-- Name: students students_code_key; Type: CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.students
    ADD CONSTRAINT students_code_key UNIQUE (code);


--
-- Name: students students_email_key; Type: CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.students
    ADD CONSTRAINT students_email_key UNIQUE (email);


--
-- Name: students students_pkey; Type: CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.students
    ADD CONSTRAINT students_pkey PRIMARY KEY (id);



--
-- Name: courses courses_instructor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_instructor_id_fkey FOREIGN KEY (instructor_id) REFERENCES public.instructors(id) ON DELETE SET NULL;


--
-- Name: courses courses_pack_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_pack_id_fkey FOREIGN KEY (pack_id) REFERENCES public.packs(id) ON DELETE CASCADE;


--
-- Name: student_course fk_course; Type: FK CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.student_course
    ADD CONSTRAINT fk_course FOREIGN KEY (course_id) REFERENCES public.courses(id) ON DELETE CASCADE;


--
-- Name: preference_items fk_preference_items_course; Type: FK CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.preference_items
    ADD CONSTRAINT fk_preference_items_course FOREIGN KEY (course_id) REFERENCES public.courses(id) ON DELETE CASCADE;


--
-- Name: preference_items fk_preference_items_pack; Type: FK CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.preference_items
    ADD CONSTRAINT fk_preference_items_pack FOREIGN KEY (pack_id) REFERENCES public.packs(id) ON DELETE SET NULL;


--
-- Name: preference_items fk_preference_items_preference; Type: FK CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.preference_items
    ADD CONSTRAINT fk_preference_items_preference FOREIGN KEY (preference_id) REFERENCES public.preferences(id) ON DELETE CASCADE;


--
-- Name: preferences fk_preferences_student; Type: FK CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.preferences
    ADD CONSTRAINT fk_preferences_student FOREIGN KEY (student_id) REFERENCES public.students(id) ON DELETE CASCADE;


--
-- Name: student_course fk_student; Type: FK CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.student_course
    ADD CONSTRAINT fk_student FOREIGN KEY (student_id) REFERENCES public.students(id) ON DELETE CASCADE;


--
-- Name: student_grades fk_student_grades_course; Type: FK CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.student_grades
    ADD CONSTRAINT fk_student_grades_course FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: student_grades fk_student_grades_student; Type: FK CONSTRAINT; Schema: public; Owner: appuser
--

ALTER TABLE ONLY public.student_grades
    ADD CONSTRAINT fk_student_grades_student FOREIGN KEY (student_id) REFERENCES public.students(id);


--
-- PostgreSQL database dump complete

