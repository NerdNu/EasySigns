
export('easysign.signs', get_value('easysign.signs'))
bind(player_interact, null, null, @e,
	if(array_index_exists(@e, 'location') && is_sign_at(@e['location'])){
		@signdata = import('easysign.signs')
		@key = json_encode(@e['location'])
		if(is_array(@signdata) && array_index_exists(@signdata, @key)){
			cancel()
			foreach(@signdata[@key], @cmd,
				@data = @cmd['data']
				switch(@cmd['type'],
					# Add new types here
					'sleep',
						set_pbed_location(ploc())
						msg(color(LIGHT_PURPLE).'Your bed has now been set!')
					,'warp',
						if (is_associative(@data)) {
							# New storage format, better for /easy-sign-info.
							set_ploc(@data['location'])
						} else {
							# Retain compatibility with old storage format.
							set_ploc(@data)
						}
					,'ci',
						foreach(0..104,@i,
							@array = associative_array()
							@array[@i] = null
							set_pinv(@array)
						)
					,'give',
						@item = @data['item']
						@qty = @data['qty']
						@slot = @data['slot']
						if(@slot != null){
							@array = associative_array()
							@array[@slot] = array(type: @item, qty: @qty)
							console(@array)
							set_pinv(@array)
						} else {
							pgive_item(@item, @qty)
						}
					,'hunger',
						set_phunger(20)
						set_psaturation(20),
					,'announce',
						@message = @data['message']
						if(reg_count('%s', @data['message'])){
							@message = sprintf(@message, player())
						}
						@value = get_value('easysign.announce')
						@id = @data['id']
						if(!is_array(@value), @value = associative_array())
						if(!array_index_exists(@value, @id)){
							@value[@id] = array()
						}
						if(array_contains(@value[@id], player())){
							msg(color(GREEN).'[SIGN] '.color(WHITE).'You can only announce here once!')
						} else {
							array_push(@value[@id], player())
							store_value('easysign.announce', @value)
							broadcast(colorize(@message))
						}
					,'heal',
						if(@data == 0){
							set_phealth(20)
						} else {
							for(@i = 0, @i <= 20 - pinfo()[5] - 1, @i++,
								@to = 1000 * @data + (@i * 1000)
								set_timeout(@to, closure(
									if(pinfo()[5] < 20){
										set_phealth(pinfo()[5] + 1)
									}
								))
							)
						}
					,'msg',
						msg(colorize(@data))
					,'take',
						if(phas_item(@data['item']) < @data['qty']){
							die(colorize(@data['failmsg']))
						}
						ptake_item(@data['item'], @data['qty'])
					,'lore',
						@held = pinv(player(), null)
						if (is_null(@held) ||
							@held['type'] != @data['item'] ||
							is_null(@held['meta']) || 
							is_null(@held['meta']['lore'])) {
							die(colorize(@data['itemmsg']))
						}
						
						@fullLore = strip_colors(array_implode(@held['meta']['lore'], ''))
						if (@fullLore != @data['lore']) {
							die(colorize(@data['itemmsg']))
						}
						
						if (@held['qty'] < @data['qty']) {
							die(colorize(@data['qtymsg']))
						}
						
						@held['qty'] = if (@held['qty'] == 0, null, @held['qty'] - 1)
						@info = pinfo()
						@entityId = @info[13]
						@heldSlot = @info[15]
						set_inventory_item(@entityId, @heldSlot, @held)
					,'potion',
						set_peffect(player(), @data['id'], @data['strength'], @data['seconds'])
					,'leather',
						# Map from inventory slot to leather item that must go there.
						@SLOT_ITEMS = array(103: 298, 102: 299, 101: 300, 100: 301)
						foreach (@data['slots'], @slot,
							# CH is somewhat picky about how the array passed to set_pinv() is set up.
							@change = associative_array()
							@change[@slot] = array(type: @SLOT_ITEMS[@slot], qty: 1)
							set_pinv(@change)
							set_armor_color(@slot, @data['rgb'])
						)
					,'inventory',
						set_pinv(@data)
					,'dropinventory',
						# drop_item() appears to add (0.5, 0, 0.5) offset to coordinates,
						# or assumes whole number coordinates.
						@loc = @data['location']
						@location = array(@loc[0] - 0.5, @loc[1], @loc[2] - 0.5)
						foreach (@data['inventory'], @item,
							if (! is_null(@item)) {
								@entity = drop_item(@location, @item)
								if (! @data['scatter']) {
									set_entity_velocity(@entity, array(0, 0, 0))
								}
							}
						)
					,'cart',
						spawn_entity(MINECART, 1, @data['location'])
					,'direction',
						pfacing(player(), @data)
				)
			)
		}
	}
)

# Every minute, check to see if all the locations are valid signs. If not,
# go ahead and delete it.
set_interval(1 * 60 * 1000, closure(
	@signdata = get_value('easysign.signs')
	if(!is_array(@signdata)){
		@signdata = array()
	}
	foreach(@signdata, @key, @data,
		if(!is_sign_at(json_decode(@key))){
			array_remove(@signdata, @key)
			store_value('easysign.signs', @signdata)
			export('easysign.signs', @signdata)
			console(color(RED).'The sign at '.@key.' is no longer a sign, so it has been deleted from easy signs')
			die()
		}
	)
))

