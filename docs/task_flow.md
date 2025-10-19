## Task Flow

A typical task flow for an emulation operation would be as follows:

1.  The user selects a saved "card profile" from a list in the UI.
2.  The UI (View) notifies the ViewModel of the user's selection.
3.  The ViewModel requests the corresponding card data from the Repository.
4.  Upon receiving the data, the ViewModel utilizes the Domain Layer to construct the final, fully formatted Track 1 and Track 2 strings, including a freshly calculated LRC for each.
5.  The ViewModel passes these track strings to the Communication Service.
6.  The Communication Service establishes a connection if one is not already active and transmits the track data to the hardware peripheral, where it is stored in volatile memory.
7.  The user activates the "spoof" function from the UI. The ViewModel sends a "transmit" command to the Communication Service.
8.  The Communication Service relays this command to the hardware.
9.  The hardware performs the emulation and sends a status update (e.g., "success," "error") back to the service, which is then propagated up to the UI for user feedback.
