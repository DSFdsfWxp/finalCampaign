#!/bin/bash

switch_version() {
    case $1 in
        o|O)
            cp -f ../Mindustry.json.original ../Mindustry.json
            ;;
        f|F)
            cp -f ../Mindustry.json.fc ../Mindustry.json
            ;;
        *)
            echo "Invalid input, please try again."
            sleep 2
            start_menu
            ;;
    esac
}

start_menu() {
    clear
    echo "Steam Mindustry Version Switcher"
    echo
    echo "Which version do you want to switch to?"
    echo "[o] Original"
    echo "[f] FinalCampaign"
    echo
    read -p "Your choice: " answer
    switch_version "$answer"
}

end_script() {
    echo
    echo "Done."
    echo "Press any key to start the game. Or you can just close the terminal."
    read -n 1 -s -r
    cd ..
    ./Mindustry "$@"
}

start_menu
end_script