
--    Logical steps
--    auditd logs event, pushes to ELK, elastalert raises an alert, alert_actioner determines what to do about the alert

--    routing layer receives the alert (elastalert runs a command using the alert as a parameter).
--    routing layer command: parses the alert's JSON
--                           looks up the alert's associated database action entry   [the db entries are generated and configured at build time]
--                           confirms that the alert satisfies the action
--                           actions the alert
--
--
--
--
--
--
--



-- We need to make an association between the following:

--  The virtual machine host              #  Hostname
--  The goals that need achieving         #  Goal ID (based on the elastalert rule name)
--  The action that needs taking          #  Configuration option on type (hacktivity, game)
--  The marking platform / target




--  therefore, routing layer needs to receive the alert data, determine which action is needed, and pass the data on
--  the alert data contains: hostname, rule name that was triggered
--  the routing layer knows: which hostnames and goals are associated with

1 host has many goals
1 goal has 1 alert_name
1 goal has one AlertAction

An AlertAction takes in data, and does something with it.

-- Decisions that need making:
-- Which tables do we need?
-- Which fields do we need?
--    goal_id
-- A scenario HAS hosts, A host HAS goals
-- A scenario HAS a marker (made up of flags)
-- How are the goals linked to the events that need making?
--        Flags and hostnames need to be sent to hacktivity.

-- Model the data that gets sent to the marking platform
-- e.g.    POST {hostname:"P-12-451-XSD", token:"flag{asdfasfd}"

