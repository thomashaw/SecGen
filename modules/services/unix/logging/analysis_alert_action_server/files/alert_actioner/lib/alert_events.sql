--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.7
-- Dumped by pg_dump version 9.5.7

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

--
-- Name: status; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE status AS ENUM (
    'todo',
    'alert_received',
    'actioned',
    'error'
);


ALTER TYPE status OWNER TO aaa_admin;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: alert_events; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE alert_events (
    id int NOT NULl,
    alert_name text NOT NULL,
    status status,
    last_actioned timestamp
);


ALTER TABLE alert_events OWNER to aaa_admin;

--
-- Name: alert_events_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE alert_events_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE alert_events_id_seq OWNER to aaa_admin;

--
-- Name: alert_events_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE alert_events_id_seq OWNED BY alert_events.id;


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY alert_events ALTER COLUMN id SET DEFAULT nextval('alert_events_id_seq'::regclass);


--
-- Data for Name: alert_events; Type: TABLE DATA; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('alert_events_id_seq', 1, true);


--
-- Name: alert_events_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY alert_events
    ADD CONSTRAINT alert_events_pkey PRIMARY KEY (id);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM aaa_admin;
GRANT ALL ON SCHEMA public TO aaa_admin;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- Name: alert_events; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE alert_events FROM PUBLIC;
REVOKE ALL ON TABLE alert_events FROM aaa_admin;
GRANT ALL ON TABLE alert_events TO aaa_admin;
GRANT ALL ON TABLE alert_events TO aaa_admin;


--
-- Name: alert_events_id_seq; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON SEQUENCE alert_events_id_seq FROM PUBLIC;
REVOKE ALL ON SEQUENCE alert_events_id_seq FROM aaa_admin;
GRANT ALL ON SEQUENCE alert_events_id_seq TO aaa_admin;
GRANT SELECT,USAGE ON SEQUENCE alert_events_id_seq TO aaa_admin;


--
-- PostgreSQL database dump complete
--

