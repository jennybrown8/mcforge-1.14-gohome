# GoHome Minecraft Forge Mod for MC 1.14+

This is built against Minecraft 1.14 with matching Forge.  My previous version was for 1.8.  There's no
version compatibility in between, for this mod.  I wanted it caught up to the latest so I could
use it again.  Work in progress right now.  I'll try to update this with a link to the jar once it's ready for 
distribution.

# Go-Home Minecraft Mod Features

The `/go home` command teleports you to Minecraft overworld's world-spawn as long as it's safe to land.  

This is highly convenient for young children or inexperienced players who are afraid to wander
and possibly get lost.  By typing `/go home` they can always return to the main base built near
world-spawn.  It's particularly useful for children on small, family-owned servers, where play is 
largely collaborative and they appreciate being able to get back to safety from anywhere,
including from the nether.

Teleport works as long as the landing spot is safe.  If it is unsafe, teleport is denied with a message.

Safe means: Solid  block to stand on, (optionally water at your feet, but solid beneath,) air to breathe, air 
above your head, and not standing in fire or lava.  No blocks intersecting the player's 
body when they land (except water), and room to jump up one block.  Normally this would be located
in an open field, at surface level, with clear signs indicating the direction to the main base.
Water at the feet is actually useful if you expect to have players coming back from the nether on fire,
as it will immediately put out their fire.

If your world spawn lands somewhere weird, or the landscape is changed, the player might be unhappily buried inside 
a hill in the dark, but they won't hit an insta-death unless a complex intentional trap is present.
The teleport will simply refuse to go, if it can't ensure player safety to a reasonable degree.

Since the home location is exactly world spawn, you can lock this region on a multi-player server so
that other players can't set traps or mess up the landing zone.  Note that when you enter a world 
the first time, you spawn within a many-block radius near, but not exactly at, world spawn.  It may 
be worthwhile to use a world editor to move the world spawn somewhere useful and more easily 
accessed, then protect it, and then build a base nearby for newly hatched players.  Be aware that if
you build a base too close to a protected world spawn, non-operator players won't be able to 
open fences and doors and chests.  So better to leave the landing zone outside in the field and 
provide a sidewalk back to base.

The `/go home` command is always present, and always points to world-spawn.  It currently cannot be changed.
(However, you can move world spawn with a world editor.)

The `/go add`, `/go rm`, `/go add-global`, and `/go rm-global` are used by operators (in multi-player) 
or by anyone (in single-player) to save additional named locations (besides "home" which is reserved).  

Global locations work cross-dimensionally, while non-global ones only work within the same dimension.
Do note that `/go home` is in fact a way to escape The End without a portal, thus adding a serious cheat
to the game.

Right now, only operators are able to add/remove named locations on multi-player servers, but 
all players can use them once added.  Any player can add locations on a single-player 
server (although I have no idea how this behaves after open-to-LAN is activated).

I wish I could make this mod take your horse with you.  Alas, there are bugs in Minecraft itself
that currently prevent this from working.  If you go home while mounted, you'll automatically
dismount the horse (or boat or pig) and then do the teleport, leaving your mount behind.
Because of the possible stress of losing a horse, the operator on a multi-player server does
receive a message with the coordinates the player left from, so they can go retrieve a 
sad and lonely horse.



# Building
`./gradlew build`

This produces a usable mod zip inside /build/libs/ which you can distribute.


# Testing

`./gradlew runClient`


# Regression Test Cases


### Testing go home:

1. World is created okay.
2. Mod registers itself with Forge.
3. Type `/go` - you should see a list of options such as home.
4. Type `/go home` - you should see either a successful teleport or a meaningful unsafe-location warning and coordinates.
5. Go to world spawn and dig it out if it's buried in a hill or something stupid like that. Make it safe.
6. Type `/go home` - you should be able to work towards a safe landing and a successful teleport.
7. Use `/go home` from the overworld successfully.
8. Use `/go home` from the nether successfully and see it change to the overworld.
9. Make the spawn location unsafe.
10. Try `/go home` from the overworld and see it blocked.
11. Try `/go home` from the nether and see it blocked.
12. Adjust the world spawn area to be safe but also have water source block at knee level.
13. Try `/go home` from the overworld and see that you safely land in the water.
14. Walk somewhere else.  Create a boat, and ride it in the water.  Try `/go home` and see it dismount, then teleport.
15. Spawn in a horse and tame it and saddle it. Try `/go home` while mounted.  You should successfully unmount and then teleport.  The server log should have a message saying a player teleported leaving a horse behind.  If 
testing on a multi-player server, any server operator logged in while the player does that should receive
a chat message indicating the same.  Non-operators should not get the message.

### Testing add + rm (rm/remove/del/delete aliases):

1. Type `/go` to list the currently known named locations.  You will do this repeatedly as you add and remove some.
2. Walk somewhere interesting. Type `/go add testbase` to add a new location where you're standing.
3. Type `/go` to list the named locations.  Was it properly added?
4. Walk somewhere else.  Type `/go testbase` to see if it takes you back to the right spot.
5. This was a same-dimension name.  Go to the nether and type `/go` - it should not be listed as an option.  
6. Try typing `/go testbase` anyway; it should give a not-found message.
7. Go back to the overworld (hint: `/go home`).  Type `/go` and see that the name is listed again.
8. Type `/go rm testbase` and then `/go` to check that it was properly deleted.  Try `/go testbase` to confirm it's really gone.

### Testing add-global + rm-global:

1. Type `/go` to list the currently known named locations.  You will do this repeatedly as you add and remove some.
2. Walk somewhere interesting. Type `/go add-global testbase` to add a new location where you're standing.
3. Type `/go` to list the named locations.  Was it properly added?
4. Walk somewhere else.  Type `/go testbase` to see if it takes you back to the right spot.
5. This was an any-dimension name.  Go to the nether and type `/go` - it should be listed as an option, with the dimension name shown for 
any other-dimension location.
6. Try typing `/go testbase` and it should cross-dimensional teleport you to the right location.
7. Type `/go rm-global testbase` and then `/go` to check that it was properly deleted.  Try `/go testbase` to confirm it's really gone.
8. Go to the nether.  Create a named location there `/go add-global nether`  Go to the overworld.  Use `/go nether` and confirm it works.
9. Type `/go rm-global nether`


### Author Info

<https://github.com/jennybrown8>

jennybrown8 @ gmail.com

This is not a high priority project for me, so while bug reports are welcome, they might be a long time in the fixing.


