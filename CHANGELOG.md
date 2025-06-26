Nametag Rendering
- New config "Show Own Nametag" will render your own nametag (rank and leaderboard badges included) when in 3rd person, default disabled

Functions
- Combat Functions
  - `time_since_last_kill` or `last_kill_ms` returns the amount of time in milliseconds since last kill
    - `includeShared` boolean argument, defaults to false. Whether the timestamp for a kill you got the sole credit for or any kill you were credited with should be returned

Fixes
- Adding war timers from chat will work once again
- Shaman totem timer will now be tracked whilst summoner attack speed buff is active
- Fixed incompatibility with Gammabright and Distant Horizons
- Black world issue when using Gammabright hopefully fixed
- Orange beacons should now always be counted
