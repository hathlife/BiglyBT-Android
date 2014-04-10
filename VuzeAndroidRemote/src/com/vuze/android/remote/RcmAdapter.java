package com.vuze.android.remote;

import java.util.*;

import org.gudy.azureus2.core3.util.DisplayFormatters;

import android.content.Context;
import android.content.res.Resources;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.aelitis.azureus.util.MapUtils;
import com.vuze.android.remote.R;

public class RcmAdapter
	extends BaseAdapter
{
	static class ViewHolder
	{
		TextView tvName;

		TextView tvInfo;
		
		TextView tvTags;
	}

	private Context context;

	private SessionInfo sessionInfo;

	private List<String> displayList = new ArrayList<>();

	private Map<String, Map> mapRCMs = new HashMap<>();

	private Object mLock = new Object();

	private Resources resources;

	private int colorBGTagType0;

	private int colorFGTagType0;

	public RcmAdapter(Context context) {
		super();
		this.context = context;
		resources = context.getResources();
		colorBGTagType0 = resources.getColor(R.color.bg_tag_type_0);
		colorFGTagType0 = resources.getColor(R.color.fg_tag_type_0);
	}

	@Override
	public int getCount() {
		return displayList.size();
	}

	@Override
	public Object getItem(int position) {
		return displayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent, false);
	}

	public void refreshView(int position, View view, ListView listView) {
		getView(position, view, listView, true);
	}

	public View getView(int position, View convertView, ViewGroup parent,
			boolean requireHolder) {
		View rowView = convertView;
		if (rowView == null) {
			if (requireHolder) {
				return null;
			}
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.row_rcm_list, parent, false);
			ViewHolder viewHolder = new ViewHolder();

			viewHolder.tvName = (TextView) rowView.findViewById(R.id.rcmrow_title);
			viewHolder.tvInfo = (TextView) rowView.findViewById(R.id.rcmrow_info);
			viewHolder.tvTags = (TextView) rowView.findViewById(R.id.rcmrow_tags);

			rowView.setTag(viewHolder);
		}

		ViewHolder holder = (ViewHolder) rowView.getTag();
		Object item = getItem(position);
		String hash = (String) item;

		Map mapRCM = (Map) mapRCMs.get(hash);

		if (holder.tvName != null) {
			String s = MapUtils.getMapString(mapRCM, "title", "");
			holder.tvName.setText(s);
		}

		if (holder.tvInfo != null) {
			long rank = MapUtils.getMapLong(mapRCM, "rank", 0);
			long size = MapUtils.getMapLong(mapRCM, "size", 0);
			long numSeeds = MapUtils.getMapLong(mapRCM, "seeds", 0);
			long numPeers = MapUtils.getMapLong(mapRCM, "peers", 0);
			String s = "Connection Strength: " + rank + " * "
					+ DisplayFormatters.formatByteCountToKiBEtc(size) + " * " + numSeeds
					+ " seeds * " + numPeers + " peers";
			long pubDate = MapUtils.getMapLong(mapRCM, "publishDate", 0);
			if (pubDate > 0) {
				s += " * Published "
						+ DateUtils.getRelativeDateTimeString(context, pubDate,
								DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS * 2, 0).toString();
			}
			long lastSeenSecs = MapUtils.getMapLong(mapRCM, "lastSeenSecs", 0);
			if (lastSeenSecs > 0) {
				s += " * Last Seen "
						+ DateUtils.getRelativeDateTimeString(context, lastSeenSecs * 1000,
								DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS * 2, 0).toString();
			}
			holder.tvInfo.setText(s);
		}

		if (holder.tvTags != null) {
			List<?> listTags = MapUtils.getMapList(mapRCM, "tags", Collections.EMPTY_LIST);
			StringBuilder sb = new StringBuilder();
			
			for (Object object : listTags) {
				if (sb.length() > 0) {
					sb.append(' ');
				}
				sb.append("| ");
				sb.append(object.toString());
				sb.append(" |");
			}
			
			SpannableString ss = new SpannableString(sb);
			String string = sb.toString();
			AndroidUtils.setSpanBubbles(ss, string, "|", holder.tvTags.getPaint(),
					colorBGTagType0, colorFGTagType0, colorBGTagType0);
			holder.tvTags.setText(ss);
		}

		return rowView;
	}

	public void setSessionInfo(SessionInfo sessionInfo) {
		this.sessionInfo = sessionInfo;
	}

	public void updateList(List<?> listRCMs) {
		if (listRCMs == null || listRCMs.isEmpty()) {
			return;
		}
		synchronized (mLock) {
			for (Object object : listRCMs) {
				Map mapRCM = (Map) object;
				String hash = MapUtils.getMapString(mapRCM, "hash", null);

				Map old = mapRCMs.put(hash, mapRCM);
				if (old == null) {
					displayList.add(hash);
				}
			}
		}
		notifyDataSetChanged();
	}
}
