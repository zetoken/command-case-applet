# Command Case Applet
A quick and simple javacard applet that helps demonstrating the behaviour of some smart cards.
It targets ISO/IEC 7816 command cases 1 to 4. 

## Supported INS

4 generic instructions are defined:
  * `INS`=`C1` handles the incoming command as a command case 1 (no `Lc`, no `Le`)
  * `INS`=`C2` handles the incoming command as a command case 2 (no `Lc`, with `Le`)
  * `INS`=`C3` handles the incoming command as a command case 3 (with `Lc`, no `Le`)
  * `INS`=`C4` handles the incoming command as a command case 4 (with `Lc` and `Le`)

They aim at remembering the incoming APDU buffer, `Lc` and `Le` values for further use. They won't do anything more. Instructions `C2` and `C4` send back the APDU buffer content in the `UDR` field.

3 dedicated instructions are also defined as command case 2 commands:
  * `INS`=`CB` retrieves the last stored APDU buffer content
  * `INS`=`CC` retrieves the last value of `Lc` field
  * `INS`=`CE` retrieves the last value of `Le` field

## Usage

This applet can be used with the [LINQPad](https://www.linqpad.net/) script defined in [this gist](https://gist.github.com/zetoken/bdd06ded7cd8e1f58e06c34d72041036) (this is my test case).

## Technical information

  * IDE used: [JCIDE](https://www.javacardos.com/tools/)
  * Minimum Java Card version: `2.1.2`
  * Applet AID: `F0 01 02 03 04 05` (*because why not?*)